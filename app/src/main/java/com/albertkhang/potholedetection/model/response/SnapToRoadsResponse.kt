package com.albertkhang.potholedetection.model.response

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

class SnapToRoadsResponse {
    @SerializedName("snappedPoints")
    var snappedPoints: List<SnappedPointResponse> = emptyList()

    class SnappedPointResponse {
        @SerializedName("location")
        var location: LatLng = LatLng(0.0, 0.0)

        @SerializedName("originalIndex")
        var originalIndex: Int = -1

        @SerializedName("placeId")
        var placeId: String = ""
        override fun toString(): String {
            return "SnappedPointResponse(location=$location, originalIndex=$originalIndex, placeId='$placeId')"
        }
    }
}