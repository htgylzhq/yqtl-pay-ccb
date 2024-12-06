package cn.yqtl.pay.ccb.util

import cn.yqtl.pay.ccb.model.Bill
import cn.yqtl.pay.ccb.model.BillDetail
import cn.yqtl.pay.ccb.model.BillSummary
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BillParser {
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /**
     * 解析账单文件
     * @param filePath 账单文件路径
     * @return 解析后的账单对象
     */
    fun parse(filePath: Path): Bill {
        val lines = Files.readAllLines(filePath)
        require(lines.size >= 2) { "Invalid bill file format" }
        
        val summary = parseSummary(lines[0])
        val details = parseDetails(lines.drop(2))
        
        return Bill(summary, details)
    }

    private fun parseSummary(line: String): BillSummary {
        val fields = line.split("\t")
        require(fields.size >= 6) { "Invalid summary line format" }
        
        return BillSummary(
            billDate = extractValue(fields[0]),
            tradeDate = extractValue(fields[1]),
            tradeCount = extractValue(fields[2]).toInt(),
            tradeAmount = BigDecimal(extractValue(fields[3])),
            fee = BigDecimal(extractValue(fields[4])),
            settleAmount = BigDecimal(extractValue(fields[5]))
        )
    }

    private fun parseDetails(lines: List<String>): List<BillDetail> {
        return lines
            .filter { it.isNotBlank() }
            .map { line -> 
                val fields = line.split("\t")
                require(fields.size >= 15) { "Invalid detail line format" }
                
                BillDetail(
                    tradeTime = LocalDateTime.parse(fields[0], DATE_TIME_FORMATTER),
                    accountDate = fields[1],
                    bankSerial = fields[2],
                    merchantSerial = fields[3],
                    orderNo = fields[4],
                    status = fields[5],
                    payerAccount = fields[6],
                    payerName = fields[7],
                    orderAmount = BigDecimal(fields[8]),
                    tradeAmount = BigDecimal(fields[9]),
                    fee = BigDecimal(fields[10]),
                    settleAmount = BigDecimal(fields[11]),
                    counterCode = fields[12],
                    channel = fields[13],
                    cardType = fields[14]
                )
            }
    }

    private fun extractValue(field: String): String {
        val startIndex = field.indexOf("[") + 1
        val endIndex = field.indexOf("]")
        require(startIndex > 0 && endIndex > startIndex) { "Invalid field format: $field" }
        return field.substring(startIndex, endIndex)
    }
}