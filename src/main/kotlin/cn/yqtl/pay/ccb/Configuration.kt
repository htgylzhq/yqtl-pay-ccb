package cn.yqtl.pay.ccb

import java.nio.charset.Charset

data class Configuration(
    val merchantId: String,
    val userId: String,
    val password: String,
    val ebsUrl: String,
    val billUrl: String,
    val charset: Charset = DEFAULT_CHARSET,
    val timeout: Int = 10_000 // 10 seconds
) {

    companion object {
        val DEFAULT_CHARSET = Charset.forName("GB18030")
    }

    override fun toString(): String = """
        {
            "merchantId": "$merchantId",
            "userId": "$userId",
            "password": "****",
            "ebsUrl": "$ebsUrl",
            "billUrl": "$billUrl",
            "charset": "${charset.name()}",
            "timeout": "${timeout}"
        }
    """.trimIndent()
}