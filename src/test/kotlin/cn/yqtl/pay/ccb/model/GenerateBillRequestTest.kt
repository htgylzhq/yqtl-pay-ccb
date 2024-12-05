package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.Configuration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class GenerateBillRequestTest {
    private val configuration = Configuration(
        merchantId = "test_merchant",
        userId = "test_user",
        password = "test_password",
        ebsUrl = "https://test.ccb.com/api",
        billUrl = "https://test.ccb.com/bill"
    )
    
    @Test
    fun `test default values`() {
        val date = LocalDate.of(2024, 3, 15)
        val request = GenerateBillRequest(configuration, date)
        
        val xml = request.toString()
        assert(xml.contains("<DATE>20240315</DATE>"))
        assert(xml.contains("<KIND>${GenerateBillRequest.KIND_SETTLED}</KIND>"))
        assert(xml.contains("<FILETYPE>${GenerateBillRequest.FILE_TYPE_TXT}</FILETYPE>"))
    }
    
    @Test
    fun `test custom values`() {
        val date = LocalDate.of(2024, 3, 15)
        val request = GenerateBillRequest(
            configuration = configuration,
            date = date,
            kind = GenerateBillRequest.KIND_UNSETTLED,
            fileType = GenerateBillRequest.FILE_TYPE_EXCEL
        )
        
        val xml = request.toString()
        assert(xml.contains("<KIND>${GenerateBillRequest.KIND_UNSETTLED}</KIND>"))
        assert(xml.contains("<FILETYPE>${GenerateBillRequest.FILE_TYPE_EXCEL}</FILETYPE>"))
    }
    
    @Test
    fun `test invalid kind value`() {
        val request = GenerateBillRequest(configuration, LocalDate.now())
        
        assertThrows<IllegalArgumentException> {
            request.kind = 2
        }
        assertThrows<IllegalArgumentException> {
            request.kind = -1
        }
    }
    
    @Test
    fun `test invalid file type value`() {
        val request = GenerateBillRequest(configuration, LocalDate.now())
        
        assertThrows<IllegalArgumentException> {
            request.fileType = 0
        }
        assertThrows<IllegalArgumentException> {
            request.fileType = 4
        }
    }
    
    @Test
    fun `test XML structure`() {
        val date = LocalDate.of(2024, 3, 15)
        val request = GenerateBillRequest(configuration, date)
        val xml = request.toString()
        
        // 验证XML基本结构
        assert(xml.contains("<?xml version=\"1.0\" encoding=\"GB18030\""))
        assert(xml.contains("<TX>"))
        assert(xml.contains("<TX_INFO>"))
        
        // 验证字段在正确的位置
        val txInfoStart = xml.indexOf("<TX_INFO>")
        val dateTagPos = xml.indexOf("<DATE>")
        val kindTagPos = xml.indexOf("<KIND>")
        val fileTypeTagPos = xml.indexOf("<FILETYPE>")
        
        assert(dateTagPos > txInfoStart)
        assert(kindTagPos > txInfoStart)
        assert(fileTypeTagPos > txInfoStart)
    }
} 