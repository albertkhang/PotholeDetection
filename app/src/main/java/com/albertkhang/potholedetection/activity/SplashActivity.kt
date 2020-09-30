package com.albertkhang.potholedetection.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.util.NetworkUtil
import com.albertkhang.potholedetection.util.PermissionUtil

class SplashActivity : AppCompatActivity() {
    private val SPLASH_SCREEN_INTERVAL = 2 // seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        runWaitingTimer()
    }

    private fun runWaitingTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent: Intent
            if (NetworkUtil.isNetworkAvailable(this@SplashActivity)) {
                if (!PermissionUtil.isGrantedPermissions(this@SplashActivity)) {
                    // do not grant permission yet
                    intent = Intent(this@SplashActivity, RequestPermissionActivity::class.java)
                } else {
                    // granted permission
                    intent = Intent(this@SplashActivity, MainActivity::class.java)
                }
            } else {
                intent = Intent(this@SplashActivity, NoConnectionActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_INTERVAL * 1000L)
    }
}