package cn.yqtl.pay.ccb.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML

object XmlUtil {
    private val xml = XML {
        indent = 2
        xmlDeclMode = XmlDeclMode.Charset
    }

    fun <T> fromXml(xmlString: String, serializer: KSerializer<T>): T {
        return xml.decodeFromString(serializer, xmlString)
    }

    fun <T> toXml(value: T, serializer: KSerializer<T>): String {
        return xml.encodeToString(serializer, value)
    }

    inline fun <reified T> fromXml(xmlString: String): T {
        return fromXml(xmlString, serializer())
    }

    inline fun <reified T> toXml(value: T): String {
        return toXml(value, serializer())
    }
}