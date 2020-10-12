package com.albertkhang.potholedetection.model.settings

import com.google.gson.annotations.SerializedName

class ILocationSetting {
    @SerializedName("min_meter_update")
    var minMeterUpdate: Int = 1

    @SerializedName("min_milliseconds_update")
    var minMillisecondsUpdate: Int = 1000
    override fun toString(): String {
        return "ILocationSetting(minMeterUpdate=$minMeterUpdate, minMillisecondsUpdate=$minMillisecondsUpdate)"
    }


}