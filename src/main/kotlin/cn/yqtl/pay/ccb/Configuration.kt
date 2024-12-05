package cn.yqtl.pay.ccb

data class Configuration(
    val merchantId: String,
    val userId: String,
    val password: String,
    val ebsUrl: String,
    val billUrl: String
) {
    override fun toString(): String = "Configuration(merchantId='$merchantId', userId='$userId', password='****', ebsUrl='$ebsUrl', billUrl='$billUrl')"
}