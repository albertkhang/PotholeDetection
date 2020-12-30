package com.albertkhang.potholedetection.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.albertkhang.potholedetection.BuildConfig
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.service.SettingsService.Companion.SETTING_SERVICE_TAG
import com.albertkhang.potholedetection.model.response.SettingsResponse
import com.albertkhang.potholedetection.notification.DetectingNotification
import com.albertkhang.potholedetection.service.SettingsService.Companion.isLogAll
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.PermissionUtil
import com.albertkhang.potholedetection.util.SettingsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkConnection()
    }

    private fun checkConnection() {
        if (NetworkUtil.isNetworkAvailable(this@SplashActivity)) {
            getSettings()
        }
    }

    private fun getPermissionIntentResult(): Intent {
        return if (!PermissionUtil.isGrantedPermissions(this@SplashActivity)) {
            // do not grant permission yet
            Intent(this@SplashActivity, RequestPermissionActivity::class.java)
        } else {
            // granted permission
            Intent(this@SplashActivity, MainActivity::class.java)
        }
    }

    private fun logAllSettings(message: String) {
        if (isLogAll) {
            Log.d(SETTING_SERVICE_TAG, message)
        }
    }

    private fun getSettings() {
        val settingsUtil = SettingsUtil()
        settingsUtil.getSettings(object : Callback<SettingsResponse> {
            override fun onResponse(
                call: Call<SettingsResponse>,
                response: Response<SettingsResponse>
            ) {
                if (response.code() == 200) {
                    val newSettings = response.body()
                    logAllSettings(newSettings.toString())

                    if (newSettings != null) {
                        if (BuildConfig.DEBUG) {
                            LocalDatabaseUtil.writeSettings(newSettings)
                        } else {
                            val currentSettings = LocalDatabaseUtil.readSettings()

                            if (currentSettings != null) {
                                if (newSettings.version > currentSettings.version) {
                                    LocalDatabaseUtil.writeSettings(newSettings)
                                    logAllSettings("Updated new settings.")
                                } else {
                                    logAllSettings("Settings have no change.")
                                }
                            } else {
                                throw Exception("Current settings is null.")
                            }
                        }
                    } else {
                        throw Exception("New settings is null.")
                    }

                    val intent = getPermissionIntentResult()
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<SettingsResponse>, throwable: Throwable) {
                Log.d(SETTING_SERVICE_TAG, throwable.message.toString())
            }

        })
    }
}