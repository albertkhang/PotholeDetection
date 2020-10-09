package com.albertkhang.potholedetection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
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
import java.text.DecimalFormat

class DetectingNotification : Service() {
    companion object {
        private const val CHANNEL_ID = "DetectingNotificationId"
        private const val TAG = "DetectingNotification"
        private const val isLogData = false

        private const val ACTION_STOP_SERVICE =
            "com.albertkhang.potholedetection.notification.stopservice"

        private val minLocalWriteIRI =
            LocalDatabaseUtil.readSettings()!!.detectNotification.minLocalWriteIRI

        private val minLocalWriteSpeed =
            LocalDatabaseUtil.readSettings()!!.detectNotification.minLocalWriteSpeed

        /**
         * Using for check the notification is starting or stopping
         */
        var isStarted = false

        private lateinit var mAccelerometerSensor: AccelerometerSensor
        private lateinit var mLocationSensor: LocationSensor

        private val decimalFormat = DecimalFormat(LocalDatabaseUtil.readSettings()!!.iriFormat)

        private fun initSensors(context: Context) {
            mAccelerometerSensor =
                object : AccelerometerSensor(context) {
                    override fun onUpdate(accelerometer: IVector3D?, gravity: IVector3D?) {
                        if (accelerometer != null && gravity != null) {
                            val data = IAGVector(accelerometer, gravity)
                            data.timestamps = System.currentTimeMillis()

                            val iri: Float =
                                decimalFormat.format(accelerometer.iri(gravity)).toFloat()

                            if (iri > minLocalWriteIRI) {
                                // TODO: save to file
                                LocalDatabaseUtil.add(
                                    context,
                                    LocalDatabaseUtil.CACHE_AG_FILE_NAME,
                                    data
                                )

//                                if (isLogData) {
//                                    Log.i(TAG, "iri $iri added")
//                                }
                            }
                        }
                    }

                }

            mLocationSensor = object : LocationSensor(context) {
                override fun onUpdate(location: Location?) {
                    if (location !== null) {
                        val data = ILocation(location)
                        data.timestamps = System.currentTimeMillis()

                        if (location.speed >= minLocalWriteSpeed) {
                            // TODO: save to file
                            LocalDatabaseUtil.add(
                                context,
                                LocalDatabaseUtil.CACHE_LOCATION_FILE_NAME,
                                data
                            )

//                            if (isLogData) {
//                                Log.i(TAG, "location $data added")
//                            }
                        }
                    }
                }

            }
        }

        fun startService(context: Context) {
            val startIntent = Intent(context, DetectingNotification::class.java)
            ContextCompat.startForegroundService(context, startIntent)
            initSensors(context)
            mAccelerometerSensor.start()
            mLocationSensor.start()
            isStarted = true
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DetectingNotification::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onDestroy() {
        mAccelerometerSensor.stop()
        mLocationSensor.stop()
        isStarted = false

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // do heavy work on a background thread
        createNotificationChannel()
        initAndStartNotification()

        // TODO: Có thể xử lý filter data trong này

        if (intent?.getStringExtra("action").equals(ACTION_STOP_SERVICE)) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun initAndStartNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, Intent(this, MainActivity::class.java), 0
        )

        val stopPendingIntent =
            PendingIntent.getService(
                this,
                0,
                Intent(this, DetectingNotification::class.java).putExtra(
                    "action",
                    ACTION_STOP_SERVICE
                ),
                0
            )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .apply {
                setContentTitle(LocalDatabaseUtil.readSettings()?.detectNotification?.contentTitle)
                setContentText(LocalDatabaseUtil.readSettings()?.detectNotification?.contentText)
                setSmallIcon(R.drawable.ic_my_location)
                setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                setContentIntent(pendingIntent)
                addAction(
                    R.color.colorWhite,
                    LocalDatabaseUtil.readSettings()?.detectNotification?.contentStop,
                    stopPendingIntent
                )
                color = ContextCompat.getColor(
                    this@DetectingNotification,
                    R.color.colorNotificationStop
                )
            }
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground Service Description"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}