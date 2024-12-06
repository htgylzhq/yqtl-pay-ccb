package cn.yqtl.pay.ccb

import cn.yqtl.pay.ccb.model.DownloadBillRequest
import cn.yqtl.pay.ccb.model.GenerateBillRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CCBPayServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var configuration: Configuration
    private val service: CCBPayService = CCBPayService()

    @BeforeEach
    fun setup() {
        // 启动模拟服务器
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // 使用模拟服务器的URL替代真实URL
        configuration = Configuration(
            merchantId = "105000080626868",
            userId = "001",
            password = "lfyzyxcx001",
            ebsUrl = mockWebServer.url("/").toString(),
            billUrl = mockWebServer.url("/").toString()
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun MockWebServer.enqueueResponse(
        body: String,
        contentType: String = "text/xml;charset=GB18030",
        charset: Charset = Configuration.DEFAULT_CHARSET,
        responseCode: Int = 200,
        additionalHeaders: Map<String, String> = emptyMap()
    ) {
        val response = MockResponse()
            .setBody(Buffer().writeString(body, charset))
            .setResponseCode(responseCode)
            .addHeader("Content-Type", contentType)
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_END)
            
        additionalHeaders.forEach { (name, value) ->
            response.addHeader(name, value)
        }
        
        this.enqueue(response)
    }

    @Test
    fun `should generate bill successfully when request date is within valid range`() {
        val mockResponse = """
            <?xml version="1.0" encoding="GB18030"?>
            <TX>
              <REQUEST_SN>173346215346842702</REQUEST_SN>
              <CUST_ID>105000080626868</CUST_ID>
              <TX_CODE>5W1005</TX_CODE>
              <RETURN_CODE>000000</RETURN_CODE>
              <RETURN_MSG></RETURN_MSG>
              <LANGUAGE>CN</LANGUAGE>
              <TX_INFO>
                <FILE_NAME>SHOP.105000080626868.20241203.20241203.20241204140658056.MmWR.zip</FILE_NAME>	
                <NOTICE></NOTICE>
              </TX_INFO>
            </TX>
        """.trimIndent()
        mockWebServer.enqueueResponse(body = mockResponse)

        val request = GenerateBillRequest(configuration, LocalDate.now().minusDays(3))
        val response = service.generateBill(request)
        assertTrue(response.isSuccess)
        assertEquals(response.result?.fileName, "SHOP.105000080626868.20241203.20241203.20241204140658056.MmWR.zip")
    }

    @Test
    fun `should fail to generate bill when request date is in future`() {
        val mockResponse = """
            <?xml version="1.0" encoding="GB18030"?>
            <TX>
              <REQUEST_SN>173346573588810285</REQUEST_SN>
              <RETURN_CODE>0130Z110C002</RETURN_CODE>
              <RETURN_MSG>终止日期不能大于当前日期，请重新输入。</RETURN_MSG>
            </TX>
        """.trimIndent()
        mockWebServer.enqueueResponse(body = mockResponse)

        val request = GenerateBillRequest(configuration, LocalDate.now().plusDays(3))
        val response = service.generateBill(request)
        assertFalse(response.isSuccess)
        assertEquals(response.returnMsg, "终止日期不能大于当前日期，请重新输入。")
    }

    @Test
    fun `test generateBill request format`() {
        val date = LocalDate.of(2024, 3, 15)
        val request = GenerateBillRequest(configuration, date)
        
        val requestXml = request.toString()
        
        // 验证基本字段
        assertTrue(requestXml.contains("<CUST_ID>${configuration.merchantId}</CUST_ID>"))
        assertTrue(requestXml.contains("<USER_ID>${configuration.userId}</USER_ID>"))
        assertTrue(requestXml.contains("<PASSWORD>${configuration.password}</PASSWORD>"))
        assertTrue(requestXml.contains("<TX_CODE>5W1005</TX_CODE>"))
        assertTrue(requestXml.contains("<DATE>20240315</DATE>"))
    }

    @Test
    fun `test downloadBill request format`() {
        val fileName = "test_bill_20240315.txt"
        val request = DownloadBillRequest(configuration, fileName)
        
        val requestXml = request.toString()
        
        // 验证基本字段
        assertTrue(requestXml.contains("<CUST_ID>${configuration.merchantId}</CUST_ID>"))
        assertTrue(requestXml.contains("<USER_ID>${configuration.userId}</USER_ID>"))
        assertTrue(requestXml.contains("<PASSWORD>${configuration.password}</PASSWORD>"))
        assertTrue(requestXml.contains("<TX_CODE>6W0111</TX_CODE>"))
        assertTrue(requestXml.contains("<SOURCE>$fileName</SOURCE>"))
        assertTrue(requestXml.contains("<FILEPATH>merchant/shls</FILEPATH>"))
        assertTrue(requestXml.contains("<LOCAL_REMOTE>0</LOCAL_REMOTE>"))
    }

    @Test
    fun `should download bill file from IIS server`(@TempDir tempDir: Path) {
        // Mock EBS server response for generating bill
        val generateMockResponse = """
            <?xml version="1.0" encoding="GB18030"?>
            <TX>
              <REQUEST_SN>173346215346842702</REQUEST_SN>
              <CUST_ID>105000080626868</CUST_ID>
              <TX_CODE>5W1005</TX_CODE>
              <RETURN_CODE>000000</RETURN_CODE>
              <RETURN_MSG></RETURN_MSG>
              <LANGUAGE>CN</LANGUAGE>
              <TX_INFO>
                <FILE_NAME>test_bill.zip</FILE_NAME>	
                <NOTICE></NOTICE>
              </TX_INFO>
            </TX>
        """.trimIndent()
        mockWebServer.enqueueResponse(generateMockResponse)

        // Mock EBS server response for downloading bill
        val downloadMockResponse = """
            <?xml version="1.0" encoding="GB18030"?>
            <TX>
              <REQUEST_SN>173346215346842703</REQUEST_SN>
              <RETURN_CODE>000000</RETURN_CODE>
              <RETURN_MSG>下载文件成功</RETURN_MSG>
            </TX>
        """.trimIndent()
        mockWebServer.enqueueResponse(downloadMockResponse)

        // Mock IIS server response for downloading file
        val fileContent = "This is test bill content"
        mockWebServer.enqueueResponse(
            body = fileContent,
            contentType = "application/octet-stream",
            additionalHeaders = mapOf(
                "Content-Disposition" to "attachment; filename=test_bill.zip"
            )
        )

        // Execute test
        val generateRequest = GenerateBillRequest(configuration, LocalDate.now().minusDays(3))
        val generateResponse = service.generateBill(generateRequest)
        assertTrue(generateResponse.isSuccess)
        
        val fileName = generateResponse.result?.fileName ?: ""
        val downloadRequest = DownloadBillRequest(configuration, fileName)
        val downloadResponse = service.downloadBill(downloadRequest)
        assertTrue(downloadResponse.isSuccess)
        
        val localFile = service.downloadBillFile(configuration, fileName, tempDir)
        
        // Verify results
        assertTrue(Files.exists(localFile))
        assertTrue(Files.size(localFile) > 0)
        assertEquals(fileContent, Files.readString(localFile))
        
        // Verify requests
        val generateRequest1 = mockWebServer.takeRequest()
        assertEquals("POST", generateRequest1.method)
        assertContains(URLDecoder.decode(generateRequest1.body.readUtf8(), StandardCharsets.UTF_8), "<TX_CODE>5W1005</TX_CODE>")

        val downloadRequest1 = mockWebServer.takeRequest()
        assertEquals("POST", downloadRequest1.method)
        assertContains(URLDecoder.decode(downloadRequest1.body.readUtf8(), StandardCharsets.UTF_8), "<TX_CODE>6W0111</TX_CODE>")
        
        val fileRequest = mockWebServer.takeRequest()
        assertEquals("GET", fileRequest.method)
        assertTrue(fileRequest.path?.contains(fileName) == true)
    }
}