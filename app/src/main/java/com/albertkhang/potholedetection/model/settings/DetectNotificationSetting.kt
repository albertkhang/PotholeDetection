package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class DetectNotificationSetting {
    @SerializedName("content_title")
    var contentTitle: String = "Đang phát hiện ổ gà, ổ voi nà."

    @SerializedName("content_text")
    var contentText: String = "Đừng tắt nà, tắt là hong chạy được đâu nà."

    @SerializedName("content_stop")
    var contentStop: String = "Dừng lun nà!"
}