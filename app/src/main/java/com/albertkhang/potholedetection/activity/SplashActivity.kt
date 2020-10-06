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
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.PermissionUtil
import com.albertkhang.potholedetection.util.SettingsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    private val SPLASH_SCREEN_TIME: Long = 2000 // milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // TODO: add settings into database
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

    private fun getSettings() {
        val settingsUtil = SettingsUtil()

//        settingsUtil.getSettingVersion(object : Callback<Int> {
//            override fun onResponse(call: Call<Int>, response: Response<Int>) {
//                if (response.code() == 200) {
//                    Log.d(SETTING_SERVICE_TAG, "version: ${response.body()}")
//
//                    val intent = getPermissionIntentResult()
//                    startActivity(intent)
//                    finish()
//                }
//            }
//
//            override fun onFailure(call: Call<Int>, throwable: Throwable) {
//                Log.d(SETTING_SERVICE_TAG, throwable.message.toString())
//            }
//
//        })

        settingsUtil.getSettings(object : Callback<ISettings> {
            override fun onResponse(call: Call<ISettings>, response: Response<ISettings>) {
                if (response.code() == 200) {
                    Log.d(SETTING_SERVICE_TAG, response.body().toString())

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