package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class DetectNotificationSetting {
    @SerializedName("content_title")
    var contentTitle: String = ""

    @SerializedName("content_text")
    var contentText: String = ""

    @SerializedName("min_local_write_iri")
    var minLocalWriteIRI: Float = 1.1f

    @SerializedName("min_local_write_speed")
    var minLocalWriteSpeed: Float = 1.38889f
}