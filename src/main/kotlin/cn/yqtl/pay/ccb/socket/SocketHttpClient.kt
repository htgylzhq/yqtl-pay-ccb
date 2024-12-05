package cn.yqtl.pay.ccb.socket

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

class SocketHttpClient(
    private val charset: Charset,
    private val connectTimeout: Int
) {
    companion object {
        private val log = LoggerFactory.getLogger(SocketHttpClient::class.java)
    }
    
    fun post(url: String, headers: Map<String, String>, body: String): String {
        val uri = URI(url)
        val socket = Socket(uri.host, uri.port).apply {
            soTimeout = connectTimeout
        }

        try {
            val request = HttpRequest(
                method = "POST",
                path = uri.path,
                host = uri.host,
                headers = headers,
                body = body
            )

            // 发送请求
            val out = socket.getOutputStream()
            out.write(request.toString().toByteArray(charset))
            out.flush()

            // 读取响应
            val inputStream = socket.getInputStream()
            val statusCode = parseStatusCode(inputStream)
            
            if (statusCode != 200) {
                throw IOException("HTTP request failed with status code: $statusCode")
            }
            
            val reader = inputStream.bufferedReader(charset)
            return parseResponse(reader)
        } finally {
            socket.close()
        }
    }

    private fun parseResponse(reader: BufferedReader): String {
        val responseBuilder = StringBuilder()
        var line: String? = null
        var emptyLineFound = false
        
        // 读取响应头
        while (reader.readLine()?.also { line = it } != null) {
            if (line!!.isEmpty()) {
                emptyLineFound = true
                break
            }
        }
        
        // 读取响应体
        if (emptyLineFound) {
            while (reader.readLine()?.also { line = it } != null) {
                responseBuilder.appendLine(line)
            }
        }

        return responseBuilder.toString().trim()
    }
    
    fun downloadFile(url: String, targetFile: Path): Path {
        val uri = URI(url)
        val socket = Socket(uri.host, uri.port).apply {
            soTimeout = connectTimeout
        }

        try {
            val request = HttpRequest(
                method = "GET",
                path = uri.path,
                host = uri.host,
                headers = mapOf(
                    "User-Agent" to "Mozilla/5.0",
                    "Connection" to "close"
                ),
                body = ""
            )

            // 发送请求
            val out = socket.getOutputStream()
            out.write(request.toString().toByteArray(charset))
            out.flush()

            // 读取响应，直接处理字节流
            val inputStream = socket.getInputStream()
            val statusCode = parseStatusCode(inputStream)
            
            if (statusCode == 200) {
                // 跳过剩余的响应头
                skipResponseHeaders(inputStream)
                
                // 将响应体写入文件
                Files.newOutputStream(targetFile).use { fileOut ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOut.write(buffer, 0, bytesRead)
                    }
                }
                return targetFile
            } else {
                throw RuntimeException("Failed to download file, status code: $statusCode")
            }
        } finally {
            socket.close()
        }
    }
    
    private fun parseStatusCode(inputStream: InputStream): Int {
        val buffer = ByteArrayOutputStream()
        
        // 先找到 HTTP/ 标记
        val httpPrefix = "HTTP/".toByteArray(charset)
        var prefixIndex = 0
        var httpFound = false
        
        while (!httpFound) {
            val byte = inputStream.read()
            if (byte == -1) break
            
            if (byte.toByte() == httpPrefix[prefixIndex]) {
                buffer.write(byte)
                prefixIndex++
                if (prefixIndex == httpPrefix.size) {
                    httpFound = true
                }
            } else {
                prefixIndex = 0
                buffer.reset()
            }
        }
        
        if (!httpFound) {
            throw IOException("HTTP response header not found")
        }
        
        // 读取状态行的剩余部分
        var lineEnd = false
        while (!lineEnd) {
            val byte = inputStream.read()
            if (byte == -1) break
            
            buffer.write(byte)
            if (byte == '\n'.code) {
                lineEnd = true
            }
        }
        
        // 解析状态码
        val statusLine = String(buffer.toByteArray(), charset).trim()
        return statusLine.split(" ")[1].toInt()
    }
    
    private fun skipResponseHeaders(inputStream: InputStream) {
        var state = 0 // 0: 初始状态, 1: 找到\r, 2: 找到\n, 3: 找到第二个\r, 4: 找到第二个\n
        
        while (state < 4) {
            val byte = inputStream.read()
            if (byte == -1) break
            
            when (byte) {
                '\r'.code -> {
                    if (state == 0 || state == 2) state++
                    else state = 1
                }
                '\n'.code -> {
                    if (state == 1) state = 2
                    else if (state == 3) state = 4
                    else state = 0
                }
                else -> state = 0
            }
        }
    }
}