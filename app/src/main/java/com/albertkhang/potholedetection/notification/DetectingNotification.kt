package com.albertkhang.potholedetection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.activity.MainActivity
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.albertkhang.potholedetection.util.SettingsUtil

class DetectingNotification : Service() {
    private val CHANNEL_ID = "DetectingNotificationId"

    companion object {
        private const val TAG = "DetectingNotification"

        /**
         * Current this foreground service status
         */
        var isStarted = false

        fun startService(context: Context) {
            val startIntent = Intent(context, DetectingNotification::class.java)
            ContextCompat.startForegroundService(context, startIntent)

            doInBackground()

            isStarted = true
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DetectingNotification::class.java)
            context.stopService(stopIntent)

            isStarted = false
        }

        private fun doInBackground() {
            var count = 0
            val timer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(TAG, (++count).toString())
                }

                override fun onFinish() {
                    Log.d(TAG, "Count done!")
                }
            }
            timer.start()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val contentTitle = LocalDatabaseUtil.readSettings()?.detectNotification?.contentTitle
        val contentText = LocalDatabaseUtil.readSettings()?.detectNotification?.contentText

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(contentTitle)
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_my_location)
            setContentIntent(pendingIntent)
        }.build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Pothole Detection Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}