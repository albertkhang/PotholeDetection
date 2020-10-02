package com.albertkhang.potholedetection.model

import com.google.gson.annotations.SerializedName

class ISettings {
    @SerializedName("version")
    var version: Int = 0

    @SerializedName("map_zoom")
    var mapZoom: Int = 0

    override fun toString(): String {
        return "ISettings(version=$version, mapZoom=$mapZoom)"
    }
}