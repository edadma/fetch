# fetch - Cross-Platform Async HTTP Client

A unified async HTTP client for Scala with identical API across **Native**, **JVM**, and **JavaScript** platforms.

## Why This Exists

Scala developers have long struggled with HTTP clients that work differently across platforms. This library provides a **single API** that works identically everywhere, using each platform's optimal HTTP implementation under the hood.

## Features

✅ **Truly cross-platform** - Identical API on Scala Native, JVM, and JavaScript  
✅ **Async by design** - Returns `Future[HttpResponse]` on all platforms  
✅ **Web standard alignment** - Uses `status`, `ok`, and web fetch error semantics  
✅ **Platform-optimized** - Best HTTP implementation for each platform:
- **Native**: libcurl (battle-tested, widely available)
- **JVM**: java.net.http (built-in since Java 11)
- **JavaScript**: fetch API (web standard, works in browser and Node.js)

✅ **HTTP methods** - GET, POST, PUT, DELETE, etc.  
✅ **Custom headers** - Authorization, Content-Type, User-Agent, etc.  
✅ **Request bodies** - JSON, form data, plain text  
✅ **Binary support** - Handles any response type (JSON, images, PDFs, etc.)  
✅ **Proper error handling** - Network errors vs HTTP errors  
✅ **Zero dependencies** - Uses platform built-ins, no external HTTP libraries  
✅ **Fully tested** - Identical test suite passes on all platforms

## Quick Start

### Installation

**Add to build.sbt:**
```scala
libraryDependencies += "io.github.edadma" %%% "fetch" % "0.0.1"
```

**Platform requirements:**
- **Native**: libcurl development headers (`sudo apt install libcurl4-openssl-dev` on Ubuntu)
- **JVM**: Java 11+ (for java.net.http)
- **JavaScript**: Any modern environment (Node.js 18+ or browsers)

### Basic Usage

```scala
import io.github.edadma.fetch.*
import scala.concurrent.ExecutionContext.Implicits.global

// Simple GET request
fetch("https://api.github.com/users/octocat").map { response =>
  if (response.ok) {
    println(s"Success: ${response.bodyAsString}")
  } else {
    println(s"HTTP error: ${response.status}")
  }
}

// Handle network errors
fetch("https://api.example.com/data").recover {
  case e: FetchException => 
    println(s"Network error: ${e.getMessage}")
}
```

### Advanced Usage

```scala
// POST with JSON
val jsonData = """{"name": "John", "email": "john@example.com"}"""
fetch(
  "https://api.example.com/users",
  "POST",
  Some(jsonData),
  Map(
    "Content-Type" -> "application/json",
    "Authorization" -> "Bearer your-token-here"
  )
).map { response =>
  if (response.ok) {
    println("User created successfully!")
  }
}

// Binary data (images, files, etc.)
fetch("https://example.com/image.jpg").map { response =>
  if (response.ok) {
    val bytes = response.body  // Array[Byte]
    // Save to file, process, etc.
  }
}

// Custom headers and methods
fetch(
  "https://api.example.com/resource/123",
  "DELETE",
  headers = Map(
    "Authorization" -> "Bearer token",
    "User-Agent" -> "MyApp/1.0"
  )
)
```

## API Reference

### Core Function

```scala
def fetch(
  url: String,
  method: String = "GET",
  body: Option[String] = None,
  headers: Map[String, String] = Map.empty
)(implicit ec: ExecutionContext): Future[HttpResponse]
```

### Response Type

```scala
case class HttpResponse(
  body: Array[Byte],          // Raw response data
  status: Int,                // HTTP status (200, 404, etc.)
  ok: Boolean                 // true for 200-299, false otherwise
):
  def bodyAsString: String                  // UTF-8 string
  def bodyAsString(charset: String): String // Custom charset
```

### Error Handling

```scala
class FetchException(message: String) extends RuntimeException(message)
```

**Error semantics:**
- **HTTP responses** (any status) → `Future.successful(HttpResponse(...))`
- **Network errors** (DNS failure, connection refused, etc.) → `Future.failed(FetchException(...))`

This matches web fetch behavior where network errors reject the promise, but HTTP error status codes resolve successfully.

## Platform Implementation Details

Each platform uses its optimal HTTP implementation:

### Scala Native
- **Implementation**: [libcurl](https://github.com/edadma/libcurl) facade
- **Async**: Wraps synchronous libcurl calls in `Future`
- **Requirements**: libcurl development headers installed

### JVM
- **Implementation**: `java.net.http.HttpClient`
- **Async**: Native async using `sendAsync()`
- **Requirements**: Java 11+ (included in most modern Scala setups)

### JavaScript
- **Implementation**: Global `fetch()` API
- **Async**: Native Promise converted to Future
- **Requirements**: Modern browser or Node.js 18+

## Cross-Platform Testing

The library includes a comprehensive test suite that runs identically on all platforms:

```bash
sbt test  # Tests all platforms
sbt fetchNative/test  # Test only Native
sbt fetchJVM/test     # Test only JVM  
sbt fetchJS/test      # Test only JavaScript
```

All tests use [httpbin.org](https://httpbin.org) for real HTTP testing.

## Contributing

Contributions welcome! The codebase is designed for simplicity:

- **`shared/`** - Common types (`HttpResponse`, `FetchException`)
- **`native/`** - Native implementation using libcurl
- **`jvm/`** - JVM implementation using java.net.http
- **`js/`** - JavaScript implementation using fetch API

Each platform implements the same `fetch` function signature with platform-specific HTTP handling.

## License

ISC License - see LICENSE file.
