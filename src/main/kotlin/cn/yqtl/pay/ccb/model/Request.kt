package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.Configuration
import kotlin.random.Random

abstract class Request protected constructor(
    open val configuration: Configuration,
    open val txCode: String,
    open val sn: String = System.currentTimeMillis().toString() + Random.nextInt(10000, 99999),
    open val language: String = "CN"
) {
    // 存储TX_INFO节点下的字段
    protected val txInfoFields: MutableMap<String, Any> = LinkedHashMap()

    protected fun putTxInfoField(key: String, value: Any?) {
        value?.let { txInfoFields[key] = it }
    }

    override fun toString(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="GB18030" standalone="yes" ?>""")
        appendLine("<TX>")
        appendLine("  <REQUEST_SN>$sn</REQUEST_SN>")
        appendLine("  <CUST_ID>${configuration.merchantId}</CUST_ID>")
        appendLine("  <USER_ID>${configuration.userId}</USER_ID>")
        appendLine("  <PASSWORD>${configuration.password}</PASSWORD>")
        appendLine("  <TX_CODE>$txCode</TX_CODE>")
        appendLine("  <LANGUAGE>$language</LANGUAGE>")
        appendLine("  <TX_INFO>")
        txInfoFields.forEach { (key, value) ->
            appendLine("    <$key>$value</$key>")
        }
        appendLine("  </TX_INFO>")
        appendLine("</TX>")
    }.trimEnd()
}