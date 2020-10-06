package com.albertkhang.potholedetection.model

import com.google.android.gms.maps.model.LatLng

class IGps : IDatabase() {
    companion object {
        const val PROVIDER_PASSIVE = "passive"
        const val PROVIDER_GPS = "gps"
        const val PROVIDER_NETWORK = "network"
    }

    /**
     * GPS providers
     *
     * @unit passive | gps | network
     */
    var provider: String = ""

    /**
     * contain latitude and longitude
     */
    var latLng: LatLng = LatLng(0.0, 0.0)

    /**
     * @unit meter
     */
    var accuracy: Float = 0.0f

    /**
     * @unit meter
     */
    var altitude: Float = 0.0f

    /**
     * @unit m/s
     */
    var speed: Float = 0.0f
}