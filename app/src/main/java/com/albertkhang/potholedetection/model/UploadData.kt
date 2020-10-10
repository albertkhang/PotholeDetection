package com.albertkhang.potholedetection.model

import com.google.android.gms.maps.model.LatLng

data class UploadData(
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    var iri: Float
)