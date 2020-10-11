package com.albertkhang.potholedetection.model

import com.google.android.gms.maps.model.LatLng

data class UploadData(
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    var iri: Float = 0f,
    var speed: Float = 0f,
    var quality: String = if (speed >= 1.38889) {
        if (iri <= 0.2f) {
            "G"
        } else if (iri <= 0.3f) {
            "A"
        } else {
            "B"
        }
    } else {
        "G"
    }
)