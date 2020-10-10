package com.albertkhang.potholedetection.model.database

import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.IDatabase

class IAGVector() : IDatabase() {
    constructor(accelerometer: IVector3D, gravity: IVector3D) : this() {
        ax = accelerometer.x
        ax = accelerometer.y
        ax = accelerometer.z

        gx = gravity.x
        gx = gravity.y
        gx = gravity.z
    }

    fun set(accelerometer: IVector3D, gravity: IVector3D) {
        ax = accelerometer.x
        ax = accelerometer.y
        ax = accelerometer.z

        gx = gravity.x
        gx = gravity.y
        gx = gravity.z
    }

    /**
     * "a" mean accelerometer
     * x, y, z is values in X-axis, Y-axis, Z-axis to make a 3D vector in space
     *
     * @unit m/s^2
     */
    var ax: Float = 0f
    var ay: Float = 0f
    var az: Float = 0f

    /**
     * "g" mean gravity
     * x, y, z is values in X-axis, Y-axis, Z-axis to make a 3D vector in space
     *
     * @unit m/s^2
     */
    var gx: Float = 0f
    var gy: Float = 0f
    var gz: Float = 0f

    var iri: Float = 0f

    override fun toString(): String {
        return "IAGVector(timestamps=$timestamps, iri=$iri)"
    }
}