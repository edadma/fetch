package io.github.edadma.fetch

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters._
import java.net.http.{HttpClient, HttpRequest, HttpResponse => JavaHttpResponse}
import java.net.URI

def fetch(
    url: String,
    method: String = "GET",
    body: Option[String] = None,
    headers: Map[String, String] = Map.empty,
)(implicit ec: ExecutionContext): Future[HttpResponse] =

  val client = HttpClient.newHttpClient()

  try {
    // Build the request
    val requestBuilder = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .method(
        method,
        body match {
          case Some(bodyData) => HttpRequest.BodyPublishers.ofString(bodyData)
          case None           => HttpRequest.BodyPublishers.noBody()
        },
      )

    // Add headers
    headers.foreach { (key, value) =>
      requestBuilder.header(key, value)
    }

    val request = requestBuilder.build()

    // Send async request and convert to Scala Future
    client.sendAsync(request, JavaHttpResponse.BodyHandlers.ofByteArray())
      .asScala
      .map { javaResponse =>
        // Convert java.net.http.HttpResponse to our HttpResponse
        val status = javaResponse.statusCode()
        HttpResponse(
          body = javaResponse.body(),
          status = status,
          ok = status >= 200 && status < 300,
        )
      }
      .recover {
        case e: java.net.http.HttpConnectTimeoutException =>
          throw new FetchException(s"Connection timeout: ${e.getMessage}")
        case e: java.net.http.HttpTimeoutException =>
          throw new FetchException(s"Request timeout: ${e.getMessage}")
        case e: java.io.IOException =>
          throw new FetchException(s"Network error: ${e.getMessage}")
        case e: IllegalArgumentException =>
          throw new FetchException(s"Invalid request: ${e.getMessage}")
        case other =>
          throw new FetchException(s"Unexpected error: ${other.getMessage}")
      }
  } catch {
    case e: java.net.URISyntaxException =>
      Future.failed(new FetchException(s"Invalid URL: ${e.getMessage}"))
    case other =>
      Future.failed(new FetchException(s"Request setup error: ${other.getMessage}"))
  }
