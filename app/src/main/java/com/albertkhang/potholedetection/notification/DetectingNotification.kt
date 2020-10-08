package com.albertkhang.potholedetection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.activity.MainActivity
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.IAGVector
import com.albertkhang.potholedetection.model.database.ILocation
import com.albertkhang.potholedetection.sensor.AccelerometerSensor
import com.albertkhang.potholedetection.sensor.LocationSensor
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.albertkhang.potholedetection.util.SettingsUtil

class DetectingNotification : Service() {
    private val CHANNEL_ID = "DetectingNotificationId"

    companion object {
        private const val TAG = "DetectingNotification"
        private lateinit var mContext: Context

        private lateinit var mAccelerometerSensor: AccelerometerSensor
        private lateinit var mLocationSensor: LocationSensor

        /**
         * Current this foreground service status
         */
        var isStarted = false

        fun init(context: Context) {
            mContext = context
            initSensors()
        }

        private fun initSensors() {
            mAccelerometerSensor =
                object : AccelerometerSensor(mContext) {
                    override fun onUpdate(accelerometer: IVector3D?, gravity: IVector3D?) {
                        if (accelerometer != null && gravity != null) {
                            val data = IAGVector(accelerometer, gravity)
                            data.timestamps = System.currentTimeMillis()

                            LocalDatabaseUtil.add(LocalDatabaseUtil.AG_VECTOR_BOOK, data)
                        }
                    }

                }

            mLocationSensor = object : LocationSensor(mContext) {
                override fun onUpdate(location: Location?) {
                    if (location !== null) {
                        val data = ILocation(location)
                        data.timestamps = System.currentTimeMillis()

                        LocalDatabaseUtil.add(LocalDatabaseUtil.LOCATION_BOOK, data)
                    }
                }

            }
        }

        fun startService() {
            val startIntent = Intent(mContext, DetectingNotification::class.java)
            ContextCompat.startForegroundService(mContext, startIntent)

            isStarted = true
            mAccelerometerSensor.start()
            mLocationSensor.start()

            doInBackground()
        }

        fun stopService() {
            val stopIntent = Intent(mContext, DetectingNotification::class.java)
            mContext.stopService(stopIntent)

            isStarted = false
            mAccelerometerSensor.stop()
            mLocationSensor.stop()

            timer.cancel()
        }

        private lateinit var timer: CountDownTimer

        private fun doInBackground() {
            var count = 0
            timer = object : CountDownTimer(10000, 1000) {
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