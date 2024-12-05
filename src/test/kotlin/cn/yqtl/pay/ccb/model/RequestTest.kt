package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.Configuration
import org.junit.jupiter.api.Test

class RequestTest {
    private val configuration = Configuration(
        merchantId = "test_merchant",
        userId = "test_user",
        password = "test_password",
        ebsUrl = "https://test.ccb.com/api",
        billUrl = "https://test.ccb.com/bill"
    )
    
    // 创建一个测试用的Request实现类
    private class TestRequest(
        configuration: Configuration,
        txCode: String = "TEST001"
    ) : Request(configuration, txCode) {
        fun addField(key: String, value: Any?) {
            putTxInfoField(key, value)
        }
    }
    
    @Test
    fun `test basic request fields`() {
        val request = TestRequest(configuration)
        val xml = request.toString()
        
        // 验证基本字段
        assert(xml.contains("<CUST_ID>test_merchant</CUST_ID>"))
        assert(xml.contains("<USER_ID>test_user</USER_ID>"))
        assert(xml.contains("<PASSWORD>test_password</PASSWORD>"))
        assert(xml.contains("<TX_CODE>TEST001</TX_CODE>"))
        assert(xml.contains("<LANGUAGE>CN</LANGUAGE>"))
    }
    
    @Test
    fun `test TX_INFO fields`() {
        val request = TestRequest(configuration)
        request.addField("TEST_KEY", "test_value")
        
        val xml = request.toString()
        assert(xml.contains("<TX_INFO>"))
        assert(xml.contains("<TEST_KEY>test_value</TEST_KEY>"))
        assert(xml.contains("</TX_INFO>"))
    }
    
    @Test
    fun `test null value handling`() {
        val request = TestRequest(configuration)
        request.addField("NULL_FIELD", null)
        
        val xml = request.toString()
        assert(!xml.contains("NULL_FIELD"))
    }
} 