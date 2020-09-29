package com.albertkhang.potholedetection.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.util.NetworkUtil
import java.util.*
import kotlin.concurrent.schedule

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
            if (NetworkUtil.isNetworkAvailable(baseContext)) {
                intent = Intent(baseContext, RequestPermissionActivity::class.java)
            } else {
                intent = Intent(baseContext, NoConnectionActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_INTERVAL * 1000L)
    }
}