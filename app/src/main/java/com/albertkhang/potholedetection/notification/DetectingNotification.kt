package com.albertkhang.potholedetection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.albertkhang.potholedetection.R
import com.albertkhang.potholedetection.activity.MainActivity
import com.albertkhang.potholedetection.broadcast.NetworkChangeReceiver
import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.entry.AccelerometerEntry
import com.albertkhang.potholedetection.model.entry.LocationEntry
import com.albertkhang.potholedetection.sensor.AccelerometerSensor
import com.albertkhang.potholedetection.sensor.LocationSensor
import com.albertkhang.potholedetection.util.*
import com.google.android.gms.maps.model.LatLng

class DetectingNotification : Service() {
    companion object {
        private const val CHANNEL_ID = "DetectingNotificationId"
        private const val TAG = "DetectingNotification"
        private const val showLog = false

        private var isRunning = false

        private const val ACTION_STOP_SERVICE =
            "com.albertkhang.potholedetection.notification.stopservice"

        /**
         * Using for check the notification is starting or stopping
         */
        var isStarted = false

        private lateinit var mAccelerometerSensor: AccelerometerSensor
        private lateinit var mLocationSensor: LocationSensor

        private lateinit var mNetworkChangeReceiver: NetworkChangeReceiver

        private fun initSensors(context: Context) {
            // the last time record speed > 10 km/h
            var lastRecordTime = 0L
            var isRecording = false

            // TODO: set setting this
            // the interval before auto stop recording
            val stopRecordingInterval = 1000 * 10 // 10 seconds

            var lastIRI = 0f

            // TODO: set setting this
            // When the phone does not move, iri < 0.05
            val minIRI = 0.05

            synchronized(isRecording) {
                mAccelerometerSensor =
                    object : AccelerometerSensor(context) {
                        override fun onUpdate(accelerometer: IVector3D, gravity: IVector3D) {
                            val iri = IRIUtil.getIRI(accelerometer, gravity)
                            Log.d(TAG, "mAccelerometerSensor onUpdate iri=$iri")
//                            Toast.makeText(context, "mAccelerometerSensor onUpdate iri=$iri", Toast.LENGTH_SHORT).show()

                            if (iri != lastIRI && iri >= minIRI) {
                                FileUtil.writeAccelerometerCache(
                                    context,
                                    AccelerometerEntry(iri)
                                )
                                lastIRI = iri
                            }
                        }
                    }

                var lastLocationEntry = LocationEntry(LatLng(0.0, 0.0), 0f)
                mLocationSensor = object : LocationSensor(context) {
                    override fun onUpdate(location: Location) {
                        Log.d(TAG, "LocationSensor onUpdate speed=${location.speed}")
//                        Toast.makeText(context, "LocationSensor onUpdate speed=${location.speed}", Toast.LENGTH_SHORT).show()

                        if (LatLng(
                                location.latitude,
                                location.longitude
                            ) == lastLocationEntry.location
                        ) return

                        // TODO: set setting this
                        val minSpeed = 2.77778 //2.77778 m/s = 10 km/h

                        if (location.speed >= minSpeed) {
                            lastRecordTime = System.currentTimeMillis()
                            isRecording = true
                            mAccelerometerSensor.start()
                        }

                        if (System.currentTimeMillis() - lastRecordTime >= stopRecordingInterval) {
                            Log.d(TAG, "not equal stopRecordingInterval")
//                            Toast.makeText(
//                                context,
//                                "not equal stopRecordingInterval",
//                                Toast.LENGTH_SHORT
//                            ).show()

                            isRecording = false
                            mAccelerometerSensor.stop()
                        } else if (isRecording) {
                            Log.d(TAG, "isRecording")
//                            Toast.makeText(context, "isRecording", Toast.LENGTH_SHORT).show()

                            lastLocationEntry = LocationEntry(
                                LatLng(location.latitude, location.longitude),
                                location.speed
                            )

                            FileUtil.writeLocationCache(
                                context, lastLocationEntry
                            )
                        }
                    }
                }
            }
        }

        fun startService(context: Context) {
            val startIntent = Intent(context, DetectingNotification::class.java)
            ContextCompat.startForegroundService(context, startIntent)
            initSensors(context)
            initNetworkChangeListener()
            mLocationSensor.start()
            isStarted = true

            isRunning = true
            val mainHandler = Handler(Looper.getMainLooper())
            val uploadTimeInterval = LocalDatabaseUtil.readSettings()!!.uploadDataInterval

//            FilterUtil.run(context)
//            FilterUtil.handleFilteredFiles(context)

            // TODO: uncomment this
            mainHandler.postDelayed(object : Runnable {
                override fun run() {
                    if (isRunning) {
                        FilterUtil.run(context)
                        mainHandler.postDelayed(this, uploadTimeInterval)
                    }
                }
            }, uploadTimeInterval)
        }

        fun stopService(context: Context) {
            isRunning = false
            val stopIntent = Intent(context, DetectingNotification::class.java)
            context.stopService(stopIntent)
        }

        private fun initNetworkChangeListener() {
            mNetworkChangeReceiver = NetworkChangeReceiver()
            mNetworkChangeReceiver.setOnNetworkChangeListener(object :
                NetworkChangeReceiver.OnNetworkChangeListener {
                override fun onNetworkChangeListener(context: Context) {
                    if (NetworkUtil.isNetworkAvailable(context)) {
                        FilterUtil.handleFilteredFiles(context)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
//        mAccelerometerSensor.stop()
        mLocationSensor.stop()
        isStarted = false

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // do heavy work on a background thread
        createNotificationChannel()
        initAndStartNotification()

        if (intent?.getStringExtra("action").equals(ACTION_STOP_SERVICE)) {
            isRunning = false
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
                NotificationManager.IMPORTANCE_HIGH
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