package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class ISettings {
    @SerializedName("version")
    var version: Int = 0

    @SerializedName("map_zoom")
    var mapZoom: Int = 0

    @SerializedName("sensor_delay")
    var sensorDelay: Int = 0

    @SerializedName("location")
    var location: ILocationSetting = ILocationSetting()

    /**
     * @unit millisecond
     */
    @SerializedName("raw_data_filter_interval")
    var rawDataFilterInterval: Int = 1000 * 60 * 60 // hour

    override fun toString(): String {
        return "ISettings(version=$version, mapZoom=$mapZoom, sensorDelay=$sensorDelay, minMeterUpdate=${location.minMeterUpdate}, minMillisecondsUpdate=${location.minMillisecondsUpdate}, rawDataFilterInterval=$rawDataFilterInterval)"
    }
}