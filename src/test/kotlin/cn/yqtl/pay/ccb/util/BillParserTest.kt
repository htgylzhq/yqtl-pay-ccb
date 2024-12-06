package cn.yqtl.pay.ccb.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillParserTest {

    private val testDataPath = Path.of(
        javaClass.classLoader.getResource(
            "sample-data/SHOP.105000080626868.20241203.20241203.20241204140658056.MmWR/SHOP.105000080626868.20241203.txt"
        )?.toURI() ?: throw IllegalStateException("测试数据文件不存在")
    )

    @Test
    fun `测试解析正常账单文件`() {
        val bill = BillParser.parse(testDataPath)

        // 验证汇总信息
        with(bill.summary) {
            assertEquals("20241203", billDate)
            assertEquals("20241203", tradeDate)
            assertEquals(1088, tradeCount)
            assertEquals(BigDecimal("559634.64"), tradeAmount)
            assertEquals(BigDecimal("0.00"), fee)
            assertEquals(BigDecimal("559634.64"), settleAmount)
        }

        // 验证明细数量
        assertEquals(1088, bill.details.size)

        // 验证第一条明细记录
        with(bill.details.first()) {
            assertEquals(LocalDateTime.parse("2024-12-03 00:07:19", BillParser.DATE_TIME_FORMATTER), tradeTime)
            assertEquals("20241203", accountDate)
            assertEquals("1010113981733155639867173", bankSerial)
            assertEquals("", merchantSerial)
            assertEquals("105000080626868268741203000700", orderNo)
            assertEquals("成功", status)
            assertEquals("88888****888888", payerAccount)
            assertEquals("o***************************", payerName)
            assertEquals(BigDecimal("2000.00"), orderAmount)
            assertEquals(BigDecimal("2000.00"), tradeAmount)
            assertEquals(BigDecimal("0.00"), fee)
            assertEquals(BigDecimal("2000.00"), settleAmount)
            assertEquals("067507881", counterCode)
            assertEquals("微信", channel)
            assertEquals("其他卡种", cardType)
        }

        // 验证最后一条明细记录
        with(bill.details.last()) {
            assertEquals(LocalDateTime.parse("2024-12-03 23:58:36", BillParser.DATE_TIME_FORMATTER), tradeTime)
            assertEquals("20241203", accountDate)
            assertEquals("1010114191733241516234629", bankSerial)
            assertEquals("", merchantSerial)
            assertEquals("ZY202412032359147590", orderNo)
            assertEquals(BigDecimal("6000.00"), tradeAmount)
            assertEquals("微信", channel)
        }
    }

    @Test
    fun `测试解析空文件时抛出异常`() {
        val emptyFilePath = Files.createTempFile("empty", ".txt")

        val exception = assertThrows<IllegalArgumentException> {
            BillParser.parse(emptyFilePath)
        }

        assertTrue(exception.message?.contains("Invalid bill file format") == true)
    }

    @Test
    fun `测试解析格式错误的汇总行时抛出异常`() {
        val invalidSummaryPath = Files.createTempFile("invalid-summary", ".txt").apply {
            // 缺少必要字段: 交易日期[20241203]
            writeText(
                """
                出单日期[20241203]	交易笔数[1]	交易金额[100.00]	手续费[0.00]	结算金额[100.00]
                表头行
            """.trimIndent()
            )
        }

        val exception = assertThrows<IllegalArgumentException> {
            BillParser.parse(invalidSummaryPath)
        }

        assertTrue(exception.message?.contains("Invalid summary line format") == true)
    }

    @Test
    fun `测试解析格式错误的明细行时抛出异常`() {
        val invalidDetailPath = Files.createTempFile("invalid-detail", ".txt").apply {
            writeText(
                """
                出单日期[20241203]	交易日期[20241203]	交易笔数[1]	交易金额[100.00]	手续费[0.00]	结算金额[100.00]
                表头行
                2024-12-03 00:07:19	不完整的明细
            """.trimIndent()
            )
        }

        val exception = assertThrows<IllegalArgumentException> {
            BillParser.parse(invalidDetailPath)
        }

        assertTrue(exception.message?.contains("Invalid detail line format") == true)
    }

    @Test
    fun `测试解析字段格式错误时抛出异常`() {
        val invalidFieldPath = Files.createTempFile("invalid-field", ".txt").apply {
            writeText(
                """
                出单日期20241203	交易日期[20241203]	交易笔数[1]	交易金额[100.00]	手续费[0.00]	结算金额[100.00]
                表头行
                2024-12-03 00:07:19	20241203	1010113981733155639867173
            """.trimIndent()
            )
        }

        val exception = assertThrows<IllegalArgumentException> {
            BillParser.parse(invalidFieldPath)
        }

        assertTrue(exception.message?.contains("Invalid field format") == true)
    }

    @Test
    fun `测试交易金额计算正确性`() {
        val bill = BillParser.parse(testDataPath)

        // 验证所有明细的交易金额之和等于汇总金额
        val totalAmount = bill.details.sumOf { it.tradeAmount }
        assertEquals(bill.summary.tradeAmount, totalAmount)
    }
}