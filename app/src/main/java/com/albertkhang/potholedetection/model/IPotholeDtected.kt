package com.albertkhang.potholedetection.model

import com.google.android.gms.maps.model.LatLng

data class IPotholeDtected(
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    var iri: Float = 0f,
    var speed: Float = 0f,
) {
    private var averageMilestone = 0.2f
        set(value) {
            field = value
        }
    private var badMilestone = 0.3f
        set(value) {
            field = value
        }

    private var minSpeed: Float = 0f
        set(value) {
            field = value
        }

    var quality: String = if (speed >= minSpeed) {
        if (iri <= averageMilestone) {
            "G"
        } else if (iri <= badMilestone) {
            "A"
        } else {
            "B"
        }
    } else {
        "G"
    }
}