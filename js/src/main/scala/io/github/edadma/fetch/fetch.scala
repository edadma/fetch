package io.github.edadma.fetch

import scala.concurrent.{Future, ExecutionContext}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.JSConverters._
import scala.util.{Success, Failure}

// JavaScript fetch API facades
@js.native
@JSGlobal("fetch")
def jsFetch(url: String, options: js.UndefOr[js.Object] = js.undefined): js.Promise[js.Dynamic] = js.native

def fetch(
    url: String,
    method: String = "GET",
    body: Option[String] = None,
    headers: Map[String, String] = Map.empty,
)(implicit ec: ExecutionContext): Future[HttpResponse] =

  // Build fetch options
  val fetchOptions = js.Dynamic.literal(
    method = method,
  )

  // Add body if provided
  body.foreach { bodyData =>
    fetchOptions.body = bodyData
  }

  // Add headers if provided
  if (headers.nonEmpty) {
    val jsHeaders = js.Dynamic.literal()
    headers.foreach { (key, value) =>
      jsHeaders.updateDynamic(key)(value)
    }
    fetchOptions.headers = jsHeaders
  }

  // Convert JavaScript Promise to Scala Future
  jsFetch(url, fetchOptions).toFuture
    .flatMap { jsResponse =>
      val status = jsResponse.status.asInstanceOf[Int]
      val ok     = jsResponse.ok.asInstanceOf[Boolean]

      // Get response body as ArrayBuffer, then convert to Array[Byte]
      jsResponse.arrayBuffer().asInstanceOf[js.Promise[js.typedarray.ArrayBuffer]].toFuture
        .map { arrayBuffer =>
          // Convert ArrayBuffer to Array[Byte]
          val uint8Array = new js.typedarray.Uint8Array(arrayBuffer)
          val byteArray  = new Array[Byte](uint8Array.length)

          var i = 0
          while (i < uint8Array.length) {
            byteArray(i) = uint8Array(i).toByte
            i += 1
          }

          HttpResponse(
            body = byteArray,
            status = status,
            ok = ok,
          )
        }
    }
    .recover {
      case e: js.JavaScriptException =>
        // Network errors, CORS errors, etc. from fetch()
        throw new FetchException(s"Network error: ${e.getMessage}")
      case other =>
        throw new FetchException(s"Unexpected error: ${other.getMessage}")
    }
