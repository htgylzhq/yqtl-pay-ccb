package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.Configuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GenerateBillRequest(
    override val configuration: Configuration,
    val date: LocalDate,
    kind: Int = KIND_SETTLED,
    fileType: Int = FILE_TYPE_TXT
) : Request(
    configuration = configuration,
    txCode = TX_CODE
) {
    companion object {
        const val TX_CODE = "5W1005"

        const val KIND_UNSETTLED = 0  // 未结流水
        const val KIND_SETTLED = 1     // 已结流水

        const val FILE_TYPE_TXT = 1    // TXT格式
        const val FILE_TYPE_EXCEL = 2  // Excel格式
        const val FILE_TYPE_BOTH = 3   // TXT+Excel
    }

    /**
     * 流水类型：0=未结流水，1=已结流水
     * @throws IllegalArgumentException 当值不是0或1时抛出
     */
    var kind: Int = kind
        set(value) {
            require(value in KIND_UNSETTLED..KIND_SETTLED) {
                "Kind must be either 0 (unsettled) or 1 (settled)"
            }
            field = value
            putTxInfoField("KIND", value)
        }

    /**
     * 文件类型：1=TXT，2=Excel，3=TXT+Excel
     * @throws IllegalArgumentException 当值不在1-3范围内时抛出
     */
    var fileType: Int = fileType
        set(value) {
            require(value in FILE_TYPE_TXT..FILE_TYPE_BOTH) {
                "FileType must be between 1 and 3"
            }
            field = value
            putTxInfoField("FILETYPE", value)
        }

    init {
        putTxInfoField("DATE", date.format(DateTimeFormatter.BASIC_ISO_DATE))
        this.kind = kind
        this.fileType = fileType
    }
}
