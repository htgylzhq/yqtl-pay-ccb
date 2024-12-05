package cn.yqtl.pay.ccb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("TX")
data class Response<T>(
    @XmlElement(true)
    @SerialName("REQUEST_SN")
    val requestSn: String = "",
    
    @XmlElement(true)
    @SerialName("CUST_ID")
    val custId: String = "",
    
    @XmlElement(true)
    @SerialName("TX_CODE")
    val txCode: String = "",
    
    @XmlElement(true)
    @SerialName("RETURN_CODE")
    val returnCode: String = "",
    
    @XmlElement(true)
    @SerialName("RETURN_MSG")
    val returnMsg: String = "",
    
    @XmlElement(true)
    @SerialName("LANGUAGE")
    val language: String = "",
    
    @XmlElement(true)
    @SerialName("TX_INFO")
    val result: T? = null
) {
    companion object {
        const val SUCCESS_CODE = "000000"
    }
    
    /**
     * 判断业务是否成功
     * @return true 表示成功，false 表示失败
     */
    val isSuccess: Boolean
        get() = returnCode == SUCCESS_CODE
}