package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class ISettings {
    @SerializedName("version")
    var version: Int = 0

    @SerializedName("map_zoom")
    var mapZoom: Int = 16

    @SerializedName("sensor_delay")
    var sensorDelay: Int = 3

    @SerializedName("location")
    var location: ILocationSetting = ILocationSetting()

    /**
     * @unit millisecond
     */
    @SerializedName("raw_data_filter_interval")
    var rawDataFilterInterval: Long = 1000 * 60 * 60 // hour

    @SerializedName("detect_notification")
    var detectNotification: DetectNotificationSetting = DetectNotificationSetting()

    @SerializedName("iri_format")
    var iriFormat: String = "#.##"
}