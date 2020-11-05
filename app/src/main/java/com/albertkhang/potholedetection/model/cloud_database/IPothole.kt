package com.albertkhang.potholedetection.model.cloud_database

import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.google.android.gms.maps.model.LatLng

data class IPothole(
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    var iri: Float = 0f,
    var speed: Float = 0f,
) {
    var quality: String = if (speed >= LocalDatabaseUtil.readSettings()!!.filterResponse.minSpeed) {
        if (iri <= LocalDatabaseUtil.readSettings()!!.filterResponse.averageMilestone) {
            "G"
        } else if (iri <= LocalDatabaseUtil.readSettings()!!.filterResponse.badMilestone) {
            "A"
        } else {
            "B"
        }
    } else {
        "G"
    }
}