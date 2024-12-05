package cn.yqtl.pay.ccb.socket

data class HttpRequest(
    val method: String,
    val path: String,
    val host: String,
    val headers: Map<String, String>,
    val body: String
) {
    override fun toString(): String = buildString {
        appendLine("$method $path HTTP/1.1")
        appendLine("Host: $host")
        headers.forEach { (key, value) ->
            appendLine("$key: $value")
        }
        appendLine()
        append(body)
    }
} 