package cn.yqtl.pay.ccb.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Bill(
    val summary: BillSummary,
    val details: List<BillDetail>
)

data class BillSummary(
    val billDate: String,         // 出单日期
    val tradeDate: String,        // 交易日期
    val tradeCount: Int,          // 交易笔数
    val tradeAmount: BigDecimal,  // 交易金额
    val fee: BigDecimal,          // 手续费
    val settleAmount: BigDecimal  // 结算金额
)

data class BillDetail(  
    val tradeTime: LocalDateTime,    // 交易时间
    val accountDate: String,         // 记账日期
    val bankSerial: String,          // 银行流水号
    val merchantSerial: String,      // 商户流水号
    val orderNo: String,             // 订单号
    val status: String,              // 订单状态
    val payerAccount: String,        // 付款方账号
    val payerName: String,           // 付款方户名
    val orderAmount: BigDecimal,     // 订单金额
    val tradeAmount: BigDecimal,     // 交易金额
    val fee: BigDecimal,             // 手续费
    val settleAmount: BigDecimal,    // 结算金额
    val counterCode: String,         // 柜台代码
    val channel: String,             // 发卡行/通道
    val cardType: String             // 支付卡种
)