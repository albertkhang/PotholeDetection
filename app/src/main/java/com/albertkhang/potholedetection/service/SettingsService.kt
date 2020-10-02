package com.albertkhang.potholedetection.service

import com.albertkhang.potholedetection.model.ISettings
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SettingsService {
    companion object {
        val SETTING_SERVICE_TAG = "SettingService"
        val SETTING_SERVICE_VERSION_TAG = "SettingServiceVersion"
        val SETTING_SERVICE_ALL_TAG = "SettingServiceAll"
        val isLogVersion = true
        val isLogAll = true
    }

    @GET("{setting_type}/version.json")
    fun getVersion(@Path("setting_type") setting_type: String): Call<Int>

    @GET("{setting_type}.json")
    fun getAll(@Path("setting_type") setting_type: String): Call<ISettings>
}