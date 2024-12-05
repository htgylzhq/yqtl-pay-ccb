package cn.yqtl.pay.ccb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("TX_INFO")
data class GenerateBillResult(
    @XmlElement(true)
    @SerialName("FILE_NAME")
    val fileName: String = "",
    
    @XmlElement(true)
    @SerialName("NOTICE")
    val notice: String = ""
)

typealias GenerateBillResponse = Response<GenerateBillResult>