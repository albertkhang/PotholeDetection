package com.albertkhang.potholedetection.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.albertkhang.potholedetection.model.IVector3D

abstract class AccelerometerSensor(context: Context) : SensorEventListener, BaseSensor {
    private var mAccelerometerSensor: Sensor
    private val mGravitySensor: Sensor
    private var mSensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var mAccelerometerVector: IVector3D? = null
    private var mGravityVector: IVector3D? = null

    companion object {
        private var x = 0f
        private var y = 0f
        private var z = 0f
    }

    init {
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    fun start() {
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        mSensorManager.unregisterListener(this)
    }

    abstract fun onUpdate(accelerometer: IVector3D?, gravity: IVector3D?)

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val type = sensorEvent.sensor.type
        if (type == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0]
            y = sensorEvent.values[1]
            z = sensorEvent.values[2]
            setAccelerometer(x, y, z)
        }
        if (type == Sensor.TYPE_GRAVITY) {
            x = sensorEvent.values[0]
            y = sensorEvent.values[1]
            z = sensorEvent.values[2]
            setGravity(x, y, z)
        }
        if (mAccelerometerVector != null && mGravityVector != null) {
            onUpdate(mAccelerometerVector, mGravityVector)
        }
    }

    private fun setAccelerometer(x: Float, y: Float, z: Float) {
        if (mAccelerometerVector == null) {
            mAccelerometerVector = IVector3D(x, y, z)
        } else {
            mAccelerometerVector!!.x = x
            mAccelerometerVector!!.y = y
            mAccelerometerVector!!.z = z
        }
    }

    private fun setGravity(x: Float, y: Float, z: Float) {
        if (mGravityVector == null) {
            mGravityVector = IVector3D(x, y, z)
        } else {
            mGravityVector!!.x = x
            mGravityVector!!.y = y
            mGravityVector!!.z = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
}