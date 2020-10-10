package com.albertkhang.potholedetection.model.database

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class ILocation() : IDatabase() {
    companion object {
        const val PROVIDER_PASSIVE = "passive"
        const val PROVIDER_GPS = "gps"
        const val PROVIDER_NETWORK = "network"
    }

    constructor(location: Location) : this() {
        provider = location.provider
        latLng = LatLng(location.latitude, location.longitude)
        accuracy = location.accuracy
        altitude = location.altitude
        speed = location.speed
    }

    fun set(location: Location) {
        provider = location.provider
        latLng = LatLng(location.latitude, location.longitude)
        accuracy = location.accuracy
        altitude = location.altitude
        speed = location.speed
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
    var accuracy: Float = 0f

    /**
     * @unit meter
     */
    var altitude: Double = 0.0

    /**
     * @unit m/s
     */
    var speed: Float = 0f

    override fun toString(): String {
        return "ILocation(timestamps=$timestamps, provider='$provider', latLng=$latLng, accuracy=$accuracy, altitude=$altitude, speed=$speed)"
    }


}