package com.albertkhang.potholedetection.model.cloud_database

import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.google.android.gms.maps.model.LatLng

data class IPothole(
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    var iri: Float = 0f,
    var speed: Float = 0f,
) {
    var quality: String = if (speed >= LocalDatabaseUtil.readSettings()!!.filter.minSpeed) {
        if (iri <= LocalDatabaseUtil.readSettings()!!.filter.averageMilestone) {
            "G"
        } else if (iri <= LocalDatabaseUtil.readSettings()!!.filter.badMilestone) {
            "A"
        } else {
            "B"
        }
    } else {
        "G"
    }
}