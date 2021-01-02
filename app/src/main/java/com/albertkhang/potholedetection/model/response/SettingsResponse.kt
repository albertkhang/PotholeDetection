package com.albertkhang.potholedetection.model.response

import com.google.gson.annotations.SerializedName

class SettingsResponse {
    @SerializedName("version")
    var version: Int = 0

    @SerializedName("map_zoom")
    var mapZoom: Int = 16

    @SerializedName("sensor_delay")
    var sensorDelay: Int = 3

    @SerializedName("location")
    var locationResponse: LocationSettingResponse = LocationSettingResponse()

    @SerializedName("detect_notification")
    var detectNotification: DetectNotificationSettingsResponse = DetectNotificationSettingsResponse()

    /**
     * @unit millisecond
     */
    @SerializedName("upload_data_interval")
    var uploadDataInterval: Long = 1000 * 60 * 60

    @SerializedName("filter")
    var filterResponse: FilterResponse = FilterResponse()

    override fun toString(): String {
        return "ISettings(version=$version, mapZoom=$mapZoom, sensorDelay=$sensorDelay, location=$locationResponse, detectNotification=$detectNotification, uploadDataInterval=$uploadDataInterval, filter=$filterResponse)"
    }

    class FilterResponse {
        @SerializedName("average_milestone")
        val averageMilestone = 0.2f

        @SerializedName("bad_milestone")
        val badMilestone = 0.3f

        @SerializedName("min_speed")
        val minSpeed: Float = 1.38889f
        override fun toString(): String {
            return "IFilter(averageMilestone=$averageMilestone, badMilestone=$badMilestone, minSpeed=$minSpeed)"
        }
    }

    class DetectNotificationSettingsResponse {
        @SerializedName("content_title")
        var contentTitle: String = "Đang phát hiện ổ gà, ổ voi nà."

        @SerializedName("content_text")
        var contentText: String = "Đừng tắt nà, tắt là hong chạy được đâu nà."

        @SerializedName("content_stop")
        var contentStop: String = "Dừng lun nà!"
        override fun toString(): String {
            return "DetectNotificationSetting(contentTitle='$contentTitle', contentText='$contentText', contentStop='$contentStop')"
        }
    }

    class LocationSettingResponse {
        @SerializedName("min_meter_update")
        var minMeterUpdate: Int = 1

        @SerializedName("min_milliseconds_update")
        var minMillisecondsUpdate: Int = 1000
        override fun toString(): String {
            return "ILocationSetting(minMeterUpdate=$minMeterUpdate, minMillisecondsUpdate=$minMillisecondsUpdate)"
        }
    }
}