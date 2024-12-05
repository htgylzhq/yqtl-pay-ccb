package cn.yqtl.pay.ccb.socket

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SocketHttpClientTest {
    private lateinit var server: ServerSocket
    private lateinit var client: SocketHttpClient
    private lateinit var executor: ExecutorService
    private val serverPort = 18080
    private val testContent = "Hello, World!"

    @BeforeEach
    fun setUp() {
        var retries = 3
        var lastException: Exception? = null
        
        while (retries > 0) {
            try {
                println("Starting server on port $serverPort")
                server = ServerSocket(serverPort)
                executor = Executors.newSingleThreadExecutor()
                executor.submit { acceptConnections() }
                // 等待服务器启动
                Thread.sleep(100)
                client = SocketHttpClient(StandardCharsets.UTF_8, 5000)
                return
            } catch (e: Exception) {
                lastException = e
                println("Failed to start server: ${e.message}")
                tearDown()
                retries--
                if (retries > 0) {
                    // 等待一段时间再重试
                    Thread.sleep(1000)
                }
            }
        }
        throw IllegalStateException("无法启动测试服务器", lastException)
    }

    private fun acceptConnections() {
        while (!server.isClosed) {
            try {
                val socket = server.accept()
                handleRequest(socket)
            } catch (e: Exception) {
                if (!server.isClosed) {
                    println("Error accepting connection: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleRequest(socket: Socket) {
        try {
            val request = socket.getInputStream().bufferedReader().readLine()
            println("Received request: $request")

            val path = request.split(" ")[1]
            when {
                path.endsWith("/download") -> handleDownload(socket)
                path.endsWith("/not-found") -> handleNotFound(socket)
                path.endsWith("/invalid") -> handleInvalid(socket)
                path.endsWith("/timeout") -> handleTimeout(socket)
                path.endsWith("/large-file") -> handleLargeFile(socket)
                path.endsWith("/empty") -> handleEmpty(socket)
                path.endsWith("/post") -> handlePost(socket)
                else -> handleNotFound(socket)
            }
        } finally {
            socket.close()
        }
    }

    private fun handleDownload(socket: Socket) {
        println("Handling /download")
        val response = buildResponse(200, testContent)
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handleNotFound(socket: Socket) {
        println("Handling /not-found")
        val response = buildResponse(404, "")
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handleInvalid(socket: Socket) {
        println("Handling /invalid")
        val response = "Invalid data before\r\n" + buildResponse(200, testContent)
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handleTimeout(socket: Socket) {
        println("Handling /timeout")
        Thread.sleep(6000)
        val response = buildResponse(200, "")
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handleLargeFile(socket: Socket) {
        println("Handling /large-file")
        val largeContent = "a".repeat(1024 * 1024)
        val response = buildResponse(200, largeContent)
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handleEmpty(socket: Socket) {
        println("Handling /empty")
        val response = buildResponse(200, "")
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun handlePost(socket: Socket) {
        println("Handling /post")
        val response = buildResponse(200, "POST request received")
        socket.getOutputStream().write(response.toByteArray())
        socket.getOutputStream().flush()
    }

    private fun buildResponse(statusCode: Int, content: String): String {
        val status = if (statusCode == 200) "200 OK" else "$statusCode Not Found"
        return "HTTP/1.1 $status\r\n" +
               "Content-Type: text/plain\r\n" +
               "Content-Length: ${content.length}\r\n" +
               "\r\n" +
               content
    }

    @AfterEach
    fun tearDown() {
        println("Stopping server...")
        server.close()
        executor.shutdown()
        println("Server stopped")
    }

    @Test
    fun `should download file successfully`(@TempDir tempDir: Path) {
        println("Starting download test")
        val targetFile = tempDir.resolve("test.txt")
        try {
            println("Attempting to download from http://localhost:$serverPort/download")
            val downloadedFile = client.downloadFile("http://localhost:$serverPort/download", targetFile)
            println("Download completed")
            
            assertTrue(Files.exists(downloadedFile))
            assertEquals(testContent, Files.readString(downloadedFile))
        } catch (e: Exception) {
            println("Download failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `should handle 404 response`(@TempDir tempDir: Path) {
        val targetFile = tempDir.resolve("test.txt")
        val exception = assertFailsWith<RuntimeException> {
            client.downloadFile("http://localhost:$serverPort/not-found", targetFile)
        }
        assertTrue(exception.message?.contains("404") == true)
    }

    @Test
    fun `should handle invalid HTTP response`(@TempDir tempDir: Path) {
        val targetFile = tempDir.resolve("test.txt")
        val downloadedFile = client.downloadFile("http://localhost:$serverPort/invalid", targetFile)
        assertTrue(Files.exists(downloadedFile))
        assertEquals(testContent, Files.readString(downloadedFile))
    }

    @Test
    fun `should handle connection timeout`(@TempDir tempDir: Path) {
        val targetFile = tempDir.resolve("test.txt")
        assertFailsWith<IOException> {
            client.downloadFile("http://localhost:$serverPort/timeout", targetFile)
        }
    }

    @Test
    fun `should download large file successfully`(@TempDir tempDir: Path) {
        val targetFile = tempDir.resolve("large.txt")
        val downloadedFile = client.downloadFile("http://localhost:$serverPort/large-file", targetFile)
        assertTrue(Files.exists(downloadedFile))
        assertEquals("a".repeat(1024 * 1024), Files.readString(downloadedFile))
    }

    @Test
    fun `should handle empty response`(@TempDir tempDir: Path) {
        val targetFile = tempDir.resolve("empty.txt")
        val downloadedFile = client.downloadFile("http://localhost:$serverPort/empty", targetFile)
        assertTrue(Files.exists(downloadedFile))
        assertEquals("", Files.readString(downloadedFile))
    }

    @Test
    fun `should post data successfully`() {
        val headers = mapOf("Content-Type" to "application/json")
        val body = """{"message": "Hello, World!"}"""
        val response = client.post("http://localhost:$serverPort/post", headers, body)
        assertEquals("POST request received", response)
    }

    @Test
    fun `should handle 404 response for post`() {
        val headers = mapOf("Content-Type" to "application/json")
        val body = """{"message": "Hello, World!"}"""
        val exception = assertFailsWith<IOException> {
            client.post("http://localhost:$serverPort/not-found", headers, body)
        }
        assertTrue(exception.message?.contains("404") == true)
    }

    @Test
    fun `should handle invalid response for post`() {
        val headers = mapOf("Content-Type" to "application/json")
        val body = """{"message": "Hello, World!"}"""
        val response = client.post("http://localhost:$serverPort/invalid", headers, body)
        assertEquals(testContent, response)
    }
}