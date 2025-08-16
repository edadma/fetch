package io.github.edadma.fetch

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.ExecutionContext

class FetchTests extends AsyncFreeSpec with Matchers {

  implicit override def executionContext: ExecutionContext = ExecutionContext.global

  "fetch" - {

    "should handle successful GET request" in {
      fetch("https://httpbin.org/get").map { response =>
        response.ok shouldBe true
        response.status shouldBe 200
        response.bodyAsString should include("httpbin.org")
      }
    }

    "should handle HTTP error status codes" in {
      fetch("https://httpbin.org/status/404").map { response =>
        response.ok shouldBe false
        response.status shouldBe 404
        response.body.length should be >= 0 // Should still get a body
      }
    }

    "should handle POST with JSON body" in {
      val jsonData = """{"name": "test", "value": 42}"""
      fetch(
        "https://httpbin.org/post",
        "POST",
        Some(jsonData),
        Map("Content-Type" -> "application/json"),
      ).map { response =>
        response.ok shouldBe true
        response.status shouldBe 200
        val responseText = response.bodyAsString
        responseText should include("test")
        responseText should include("42")
      }
    }

    "should handle custom headers" in {
      fetch(
        "https://httpbin.org/headers",
        headers = Map(
          "Authorization" -> "Bearer test-token",
          "Custom-Header" -> "test-value",
        ),
      ).map { response =>
        response.ok shouldBe true
        response.status shouldBe 200
        val responseText = response.bodyAsString
        responseText should include("Bearer test-token")
        responseText should include("test-value")
      }
    }

    "should handle server errors" in {
      fetch("https://httpbin.org/status/500").map { response =>
        response.ok shouldBe false
        response.status shouldBe 500
      }
    }

    "should debug network errors" in {
      fetch("https://nonexistent-domain-12345.com").map { response =>
        println(s"Unexpected success: status=${response.status}, ok=${response.ok}")
        println(s"Body: ${response.bodyAsString}")
        fail("Expected this to throw an exception")
      }.recover {
        case e: FetchException =>
          println(s"Got expected FetchException: ${e.getMessage}")
          succeed
        case other =>
          println(s"Got unexpected error: ${other.getClass.getSimpleName}: ${other.getMessage}")
          fail(s"Wrong exception type: $other")
      }
    }
  }
}
