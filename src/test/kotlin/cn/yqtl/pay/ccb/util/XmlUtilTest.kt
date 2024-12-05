package cn.yqtl.pay.ccb.util

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class XmlUtilTest {

    @Serializable
    @XmlSerialName("TestData")
    data class TestData(
        @XmlElement(true)
        val id: Int,

        @XmlElement(true)
        val name: String,

        @XmlElement(true)
        val active: Boolean
    )

    @Test
    fun `should serialize object to XML string`() {
        // given
        val testData = TestData(1, "测试", true)
        
        // when
        val xmlString = XmlUtil.toXml(testData)
        
        // then
        val expected = """<?xml version="1.1" encoding="UTF-8"?>
            |<TestData>
            |  <id>1</id>
            |  <name>测试</name>
            |  <active>true</active>
            |</TestData>""".trimMargin()
        assertEquals(expected, xmlString)
    }

    @Test
    fun `should deserialize XML string to object`() {
        // given
        val xmlString = """<?xml version="1.1" encoding="UTF-8"?>
            |<TestData>
            |  <id>1</id>
            |  <name>测试</name>
            |  <active>true</active>
            |</TestData>""".trimMargin()
        
        // when
        val result = XmlUtil.fromXml<TestData>(xmlString)
        
        // then
        val expected = TestData(1, "测试", true)
        assertEquals(expected, result)
    }
} 