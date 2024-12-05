package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.util.XmlUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class GenerateBillResultTest {

    @Test
    fun `should create GenerateBillResult with default values`() {
        val result = GenerateBillResult()
        
        assertEquals("", result.fileName)
        assertEquals("", result.notice)
    }

    @Test
    fun `should create GenerateBillResult with all values`() {
        val result = GenerateBillResult(
            fileName = "test.pdf",
            notice = "测试通知"
        )
        
        assertEquals("test.pdf", result.fileName)
        assertEquals("测试通知", result.notice)
    }

    @Test
    fun `should serialize and deserialize GenerateBillResponse correctly`() {
        // given
        val result = GenerateBillResult("test.pdf", "测试通知")
        val response = Response(
            requestSn = "REQ123",
            custId = "CUST456",
            txCode = "TX789",
            returnCode = "000000",
            returnMsg = "",
            language = "CN",
            result = result
        )

        // when
        val xml = XmlUtil.toXml(response)
        println(xml)

        // then
        val deserializedResponse = XmlUtil.fromXml<GenerateBillResponse>(xml)

        assertEquals(response.requestSn, deserializedResponse.requestSn)
        assertEquals(response.custId, deserializedResponse.custId)
        assertEquals(response.txCode, deserializedResponse.txCode)
        assertEquals(response.returnCode, deserializedResponse.returnCode)
        assertEquals(response.returnMsg, deserializedResponse.returnMsg)
        assertEquals(response.language, deserializedResponse.language)
        assertEquals(response.result?.fileName, deserializedResponse.result?.fileName)
        assertEquals(response.result?.notice, deserializedResponse.result?.notice)
    }

    @Test
    fun `should handle null result in GenerateBillResponse`() {
        // given
        val response = Response<GenerateBillResult>(
            requestSn = "REQ123",
            custId = "CUST456",
            txCode = "TX789",
            returnCode = "100000",
            returnMsg = "",
            language = "CN",
            result = null
        )

        // when
        val xml = XmlUtil.toXml(response)
        println(xml)

        // then
        val deserializedResponse = XmlUtil.fromXml<GenerateBillResponse>(xml)

        assertEquals(response.requestSn, deserializedResponse.requestSn)
        assertNull(deserializedResponse.result)
        assertFalse(deserializedResponse.isSuccess)
    }
} 