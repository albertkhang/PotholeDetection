package com.albertkhang.potholedetection.service

import com.albertkhang.potholedetection.model.response.SettingsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SettingsService {
    companion object {
        const val SETTING_SERVICE_TAG = "SettingService"
        const val SETTING_SERVICE_VERSION_TAG = "SettingServiceVersion"
        const val SETTING_SERVICE_ALL_TAG = "SettingServiceAll"
        const val isLogVersion = false
        const val isLogAll = false
    }

    @GET("{setting_type}/version.json")
    fun getVersion(@Path("setting_type") setting_type: String): Call<Int>

    @GET("{setting_type}.json")
    fun getAll(@Path("setting_type") setting_type: String): Call<SettingsResponse>
}