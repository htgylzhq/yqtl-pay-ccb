package cn.yqtl.pay.ccb

import cn.yqtl.pay.ccb.model.*
import cn.yqtl.pay.ccb.socket.SocketHttpClient
import cn.yqtl.pay.ccb.util.XmlUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.Path

class CCBPayService {
    companion object {
        private val log = LoggerFactory.getLogger(CCBPayService::class.java)
        private val CHARSET: Charset = Charset.forName("GB18030")
        private const val CONNECT_TIMEOUT = 10_000 // 10 seconds
    }

    private fun sendToEBS(request: Request): String {
        val requestXml = request.toString()
        log.debug("CCB_EBS_Request: {}", requestXml)
        
        val params = "requestXml=${URLEncoder.encode(requestXml, CHARSET)}"

        val headers = mapOf(
            "Content-Type" to "application/x-www-form-urlencoded",
            "Content-Length" to params.length.toString(),
            "Connection" to "close"
        )

        val response = SocketHttpClient(
            charset = CHARSET,
            connectTimeout = CONNECT_TIMEOUT
        ).post(
            url = request.configuration.ebsUrl,
            headers = headers,
            body = params
        )
        
        log.debug("CCB_EBS_Response: {}", response)
        return response
    }

    fun generateBill(request: GenerateBillRequest): GenerateBillResponse {
        val responseXml = sendToEBS(request)
        return XmlUtil.fromXml(responseXml)
    }

    fun downloadBill(
        request: DownloadBillRequest
    ): DownloadBillResponse {        
        val responseXml = sendToEBS(request)
        return XmlUtil.fromXml(responseXml)
    }

    /**
     * 从外联客户端服务器下载账单文件到本地
     * @param configuration 配置信息
     * @param fileName 账单文件名，从 generateBill 的响应中获取
     * @param targetDirectory 本地保存目录
     * @return 下载后的本地文件路径
     */
    fun downloadBillFile(configuration: Configuration, fileName: String, targetDirectory: Path): Path {
        // 构建下载URL
        val encodedFileName = URLEncoder.encode(fileName, CHARSET)
        val downloadUrl = "${configuration.billUrl}/$encodedFileName"
        
        log.debug("Downloading bill file from: {}", downloadUrl)
        
        val targetFile = targetDirectory.resolve(fileName)
        var attempts = 0
        val maxAttempts = 3
        var lastException: Exception? = null
        
        while (attempts < maxAttempts) {
            try {
                val client = SocketHttpClient(
                    charset = CHARSET,
                    connectTimeout = CONNECT_TIMEOUT
                )
                
                val path = client.downloadFile(downloadUrl, targetFile)
                log.debug("Downloaded bill file to: {}", path.toAbsolutePath())
                return path
            } catch (e: Exception) {
                lastException = e
                log.warn("Attempt ${attempts + 1} failed to download file", e)
            }
            
            attempts++
            if (attempts < maxAttempts) {
                Thread.sleep(2000L * attempts)
            }
        }
        
        throw IOException("Failed to download bill file after $maxAttempts attempts", lastException)
    }
}