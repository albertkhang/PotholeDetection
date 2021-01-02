package com.albertkhang.potholedetection.model.entry

import com.google.android.gms.maps.model.LatLng

/**
 * @Type Local Database Entry
 *
 * @Description be used to contain the location data when handling
 */
data class LocationEntry(
    var location: LatLng, val speed: Float // @unit meter/second
) : LocalEntry() {
    override fun toString(): String {
        return "LocationEntry(timestamp=${timestamp}, location=$location, speed=$speed)"
    }
}