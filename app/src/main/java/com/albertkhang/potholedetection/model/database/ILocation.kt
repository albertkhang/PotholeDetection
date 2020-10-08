package com.albertkhang.potholedetection.model.database

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class ILocation(location: Location) : IDatabase() {
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
    var provider: String = location.provider

    /**
     * contain latitude and longitude
     */
    var latLng: LatLng = LatLng(location.latitude, location.longitude)

    /**
     * @unit meter
     */
    var accuracy: Float = location.accuracy

    /**
     * @unit meter
     */
    var altitude: Double = location.altitude

    /**
     * @unit m/s
     */
    var speed: Float = location.speed
}