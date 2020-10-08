package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class DetectNotificationSetting {
    @SerializedName("content_title")
    var contentTitle: String = ""

    @SerializedName("content_text")
    var contentText: String = ""
}