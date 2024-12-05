package cn.yqtl.pay.ccb

import cn.yqtl.pay.ccb.model.DownloadBillRequest
import cn.yqtl.pay.ccb.model.GenerateBillRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CCBPayServiceTest {

    private val configuration: Configuration = Configuration(
        merchantId = "105000080626868",
        userId = "001",
        password = "lfyzyxcx001",
        ebsUrl = "http://127.0.0.1:10004",
        billUrl = "http://127.0.0.1:10005"
    )

    private val service: CCBPayService = CCBPayService()

    @Test
    fun `should generate bill successfully when request date is within valid range`() {
        val request = GenerateBillRequest(configuration, LocalDate.now().minusDays(3))
        val response = service.generateBill(request)
        assertTrue(response.isSuccess)
    }

    @Test
    fun `should fail to generate bill when request date is in future`() {
        val request = GenerateBillRequest(configuration, LocalDate.now().plusDays(3))
        val response = service.generateBill(request)
        assertFalse(response.isSuccess)
        assertTrue(response.returnMsg.isNotEmpty())
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
        // First generate a bill
        val generateRequest = GenerateBillRequest(configuration, LocalDate.now().minusDays(3))
        val generateResponse = service.generateBill(generateRequest)
        assertTrue(generateResponse.isSuccess)
        
        // Then download the bill to EBS server
        val fileName = generateResponse.result?.fileName ?: ""
        val downloadRequest = DownloadBillRequest(configuration, fileName)
        val downloadResponse = service.downloadBill(downloadRequest)
        assertTrue(downloadResponse.isSuccess)
        
        // Finally download the bill file from IIS server to local
        val localFile = service.downloadBillFile(configuration, fileName, tempDir)
        assertTrue(Files.exists(localFile))
        assertTrue(Files.size(localFile) > 0)

        Files.copy(localFile, Path.of("/Users/rochuukyou/Developer/Projects/yqtl-pay-ccb", fileName),
            StandardCopyOption.REPLACE_EXISTING)
    }
}