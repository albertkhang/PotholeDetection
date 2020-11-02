package com.albertkhang.potholedetection.model.entry

import com.google.android.gms.maps.model.LatLng

/**
 * @Type Cloud Database Entry
 *
 * @Description A list of roads is not a good road
 */
data class RoadEntry(
    val location: LatLng,
    val placeId: String,
    val iri: Float
)