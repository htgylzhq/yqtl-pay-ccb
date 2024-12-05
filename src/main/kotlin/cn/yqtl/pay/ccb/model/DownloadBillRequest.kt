package cn.yqtl.pay.ccb.model

import cn.yqtl.pay.ccb.Configuration

class DownloadBillRequest(
    override val configuration: Configuration,
    fileName: String,
) : Request(
    configuration = configuration,
    txCode = TX_CODE
) {
    companion object {
        const val TX_CODE = "6W0111"
        const val FILE_PATH = "merchant/shls"
        const val LOCAL_REMOTE = 0
    }

    /**
     * 文件名
     */
    var fileName: String = fileName
        set(value) {
            field = value
            putTxInfoField("SOURCE", value)
        }

    init {
        this.fileName = fileName
        putTxInfoField("FILEPATH", FILE_PATH)
        putTxInfoField("LOCAL_REMOTE", LOCAL_REMOTE)
    }
}