package com.albertkhang.potholedetection.service

import com.albertkhang.potholedetection.model.response.SettingsResponse
import com.albertkhang.potholedetection.model.response.SnapToRoadsResponse
import retrofit2.Call
import retrofit2.http.*

interface SnapToRoadsService {
    companion object {
        const val URL = "https://roads.googleapis.com/v1/"
        private const val key = "AIzaSyCeoKznJ27hXX50hXTz43XxXyeAZQS265g"
    }

    /**
     * @maxPoints 100
     */
    @GET("snapToRoads?interpolate=true&key=$key")
    fun get(@Query("path", encoded = true) points: String): Call<SnapToRoadsResponse>
}