package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class DetectNotificationSetting {
    @SerializedName("content_title")
    var contentTitle: String = "Đang phát hiện ổ gà, ổ voi nà."

    @SerializedName("content_text")
    var contentText: String = "Đừng tắt nà, tắt là hong chạy được đâu nà."

    @SerializedName("content_stop")
    var contentStop: String = "Dừng lun nà!"

    @SerializedName("min_local_write_iri")
    var minLocalWriteIRI: Float = 0.3f

    @SerializedName("min_local_write_speed")
    var minLocalWriteSpeed: Float = 1.38889f
}