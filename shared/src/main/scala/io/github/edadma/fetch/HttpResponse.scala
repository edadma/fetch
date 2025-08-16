package io.github.edadma.fetch

// Response type
case class HttpResponse(
    body: Array[Byte],
    status: Int,
    ok: Boolean,
):
  def bodyAsString: String                  = new String(body, "UTF-8")
  def bodyAsString(charset: String): String = new String(body, charset)

// Exception type for network/parsing errors
class FetchException(message: String) extends RuntimeException(message)
