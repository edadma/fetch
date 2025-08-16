package io.github.edadma.fetch

import scala.concurrent.{Future, ExecutionContext}

def fetch(
    url: String,
    method: String = "GET",
    body: Option[String] = None,
    headers: Map[String, String] = Map.empty,
)(implicit ec: ExecutionContext): Future[HttpResponse] =
  Future {
    // Use the published libcurl library
    val libcurlResponse = io.github.edadma.libcurl.fetch(url, method, body, headers)

    // Convert from libcurl types to our shared types
    HttpResponse(
      body = libcurlResponse.body,
      status = libcurlResponse.status,
      ok = libcurlResponse.ok,
    )
  }.recover {
    case e: io.github.edadma.libcurl.FetchException =>
      throw new FetchException(e.getMessage)
    case other =>
      throw new FetchException(s"Unexpected error: ${other.getMessage}")
  }
