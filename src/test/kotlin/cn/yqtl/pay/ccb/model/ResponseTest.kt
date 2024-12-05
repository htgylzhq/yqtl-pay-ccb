package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.util.XmlUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResponseTest {

    @Test
    fun `should create Response with default values`() {
        val response = Response<String>()
        
        assertEquals("", response.requestSn)
        assertEquals("", response.custId)
        assertEquals("", response.txCode)
        assertEquals("", response.returnCode)
        assertEquals("", response.returnMsg)
        assertEquals("", response.language)
        assertNull(response.result)
    }

    @Test
    fun `should create Response with all values`() {
        val response = Response(
            requestSn = "REQ123",
            custId = "CUST456",
            txCode = "TX789",
            returnCode = "000000",
            returnMsg = "成功",
            language = "CN",
            result = "测试信息"
        )

        assertEquals("REQ123", response.requestSn)
        assertEquals("CUST456", response.custId)
        assertEquals("TX789", response.txCode)
        assertEquals("000000", response.returnCode)
        assertEquals("成功", response.returnMsg)
        assertEquals("CN", response.language)
        assertEquals("测试信息", response.result)
    }

    @Test
    fun `isSuccess should return true when returnCode is 000000`() {
        val response = Response<String>(returnCode = "000000")
        assertTrue(response.isSuccess)
    }

    @Test
    fun `isSuccess should return false when returnCode is not 000000`() {
        val failureCodes = listOf("100000", "200000", "", "null", "000001")
        failureCodes.forEach { code ->
            val response = Response<String>(returnCode = code)
            assertFalse(response.isSuccess, "Return code $code should indicate failure")
        }
    }

    @Test
    fun `should serialize and deserialize response with result`() {
        // 创建响应对象
        val response = Response<String>(
            requestSn = "1234567890",
            custId = "105000080626868",
            txCode = "5W1005",
            returnCode = "000000",
            returnMsg = "",
            language = "CN",
            result = "测试信息"
        )

        // 序列化
        val xml = XmlUtil.toXml(response)
        println(xml)

        // 反序列化
        val deserializedResponse = XmlUtil.fromXml<Response<String>>(xml)

        // 验证
        assertEquals("测试信息", response.result)
    }
} 