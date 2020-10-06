package com.albertkhang.potholedetection.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.service.SettingsService.Companion.SETTING_SERVICE_TAG
import com.albertkhang.potholedetection.model.ISettings
import com.albertkhang.potholedetection.service.SettingsService.Companion.isLogAll
import com.albertkhang.potholedetection.util.DatabaseUtil
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.PermissionUtil
import com.albertkhang.potholedetection.util.SettingsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    private val SPLASH_SCREEN_TIME: Long = 1000 // milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkConnection()
    }

    private fun checkConnection() {
        if (NetworkUtil.isNetworkAvailable(this@SplashActivity)) {
            getSettings()
        } else {
            showNoConnectionActivityAfterAWhile()
        }
    }

    private fun getPermissionIntentResult(): Intent {
        val intent: Intent
        if (!PermissionUtil.isGrantedPermissions(this@SplashActivity)) {
            // do not grant permission yet
            intent = Intent(this@SplashActivity, RequestPermissionActivity::class.java)
        } else {
            // granted permission
            intent = Intent(this@SplashActivity, MainActivity::class.java)
        }

        return intent
    }

    private fun logAllSettings(message: String) {
        if (isLogAll) {
            Log.d(SETTING_SERVICE_TAG, message)
        }
    }

    private fun getSettings() {
        val settingsUtil = SettingsUtil()
        settingsUtil.getSettings(object : Callback<ISettings> {
            override fun onResponse(call: Call<ISettings>, response: Response<ISettings>) {
                if (response.code() == 200) {
                    val newSettings = response.body()
                    logAllSettings(newSettings.toString())

                    if (newSettings != null) {
                        if (SettingsUtil.isDebugVersion) {
                            DatabaseUtil.writeSettings(newSettings)
                        } else {
                            val currentSettings = DatabaseUtil.readSettings()

                            if (currentSettings != null) {
                                if (newSettings.version > currentSettings.version) {
                                    DatabaseUtil.writeSettings(newSettings)
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

            override fun onFailure(call: Call<ISettings>, throwable: Throwable) {
                Log.d(SETTING_SERVICE_TAG, throwable.message.toString())
            }

        })
    }

    private fun showNoConnectionActivityAfterAWhile() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, NoConnectionActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_TIME)
    }
}