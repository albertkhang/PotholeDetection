package com.albertkhang.potholedetection.util

import android.os.Build
import com.albertkhang.potholedetection.BuildConfig
import com.albertkhang.potholedetection.service.SettingsService
import com.albertkhang.potholedetection.model.response.SettingsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingsUtil {
    companion object {
        private const val DATABASE_URL: String = "https://promising-env-291106.firebaseio.com/"
        private const val DEBUG_SETTINGS: String = "debug-settings"
        private const val RELEASE_SETTINGS: String = "release-settings"
        private lateinit var mSettingsService: SettingsService
    }

    /**
     * Return settings version
     *
     * Debug 99999999
     * Release year month day
     *
     * @callback onResponse
     * @callback onFailure
     */
    fun getSettingVersion(callback: Callback<Int>) {
        mSettingsService.getVersion(getCurrentSettingType()).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<Int>, throwable: Throwable) {
                callback.onFailure(call, throwable)
            }
        })
    }

    /**
     * Return debug settings or release settings base on current settings type
     *
     * @callback onResponse
     * @callback onFailure
     */
    fun getSettings(callback: Callback<SettingsResponse>) {
        mSettingsService.getAll(getCurrentSettingType())
            .enqueue(object : Callback<SettingsResponse> {
                override fun onResponse(
                    call: Call<SettingsResponse>,
                    response: Response<SettingsResponse>
                ) {
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<SettingsResponse>, throwable: Throwable) {
                    callback.onFailure(call, throwable)
                }
            })
    }

    /**
     * Return type of current settings
     * Debug or Release
     *
     * @return settings type
     */
    private fun getCurrentSettingType(): String {

        return if (BuildConfig.DEBUG) DEBUG_SETTINGS else RELEASE_SETTINGS
    }

    init {
        initRetrofit()
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(DATABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mSettingsService = retrofit.create(SettingsService::class.java)
    }
}