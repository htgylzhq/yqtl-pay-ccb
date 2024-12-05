package cn.yqtl.pay.ccb

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ConfigurationTest {

    @Test
    fun `test configuration creation and property access`() {
        val config = Configuration(
            merchantId = "test_merchant",
            userId = "test_user",
            password = "test_password",
            ebsUrl = "https://test.ccb.com/api",
            billUrl = "https://test.ccb.com/bill"
        )

        assertEquals("test_merchant", config.merchantId)
        assertEquals("test_user", config.userId)
        assertEquals("test_password", config.password)
        assertEquals("https://test.ccb.com/api", config.ebsUrl)
        assertEquals("https://test.ccb.com/bill", config.billUrl)
    }

    @Test
    fun `test data class equals`() {
        val config1 = Configuration(
            "merchant", "user", "pass", "https://test.ccb.com/api", "https://test.ccb.com/bill"
        )
        val config2 = Configuration(
            "merchant", "user", "pass", "https://test.ccb.com/api", "https://test.ccb.com/bill"
        )
        val config3 = Configuration(
            "different", "user", "pass", "https://test.ccb.com/api", "https://test.ccb.com/bill"
        )

        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun `test data class copy`() {
        val original = Configuration(
            "merchant", "user", "pass", "https://test.ccb.com/api", "https://test.ccb.com/bill"
        )
        val copied = original.copy(
            userId = "new_user",
            ebsUrl = "https://prod.ccb.com/api",
            billUrl = "https://prod.ccb.com/bill"
        )

        assertEquals("merchant", copied.merchantId)
        assertEquals("new_user", copied.userId)
        assertEquals("pass", copied.password)
        assertEquals("https://prod.ccb.com/api", copied.ebsUrl)
        assertEquals("https://prod.ccb.com/bill", copied.billUrl)
        assertNotEquals(original, copied)
    }

    @Test
    fun `test toString does not expose sensitive information`() {
        val config = Configuration(
            merchantId = "test_merchant",
            userId = "test_user",
            password = "sensitive_password",
            ebsUrl = "https://test.ccb.com/api",
            billUrl = "https://test.ccb.com/bill"
        )

        val toString = config.toString()
        assert(toString.contains("test_merchant"))
        assert(toString.contains("test_user"))
        assert(toString.contains("https://test.ccb.com/api"))
        assert(toString.contains("https://test.ccb.com/bill"))
        // 密码不应该明文出现在toString中
        assert(!toString.contains("sensitive_password"))
        assert(toString.contains("****"))
    }
}