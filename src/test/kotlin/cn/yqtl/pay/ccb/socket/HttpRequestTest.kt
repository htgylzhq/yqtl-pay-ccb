package cn.yqtl.pay.ccb.socket

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class HttpRequestTest {
    
    @Test
    fun `test request toString format`() {
        val request = HttpRequest(
            method = "POST",
            path = "/test",
            host = "example.com",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to "10"
            ),
            body = "test body"
        )
        
        val result = request.toString()
        
        // 验证请求行
        assertTrue(result.startsWith("POST /test HTTP/1.1"))
        
        // 验证 Host 头
        assertTrue(result.contains("Host: example.com"))
        
        // 验证其他头部
        assertTrue(result.contains("Content-Type: application/json"))
        assertTrue(result.contains("Content-Length: 10"))
        
        // 验证空行和请求体
        assertTrue(result.contains("\n\ntest body"))
    }
} 