package com.albertkhang.potholedetection.model.database

import com.albertkhang.potholedetection.model.IVector3D
import com.albertkhang.potholedetection.model.database.IDatabase

class IAGVector(accelerometer: IVector3D, gravity: IVector3D) : IDatabase() {
    /**
     * "a" mean accelerometer
     * x, y, z is values in X-axis, Y-axis, Z-axis to make a 3D vector in space
     *
     * @unit m/s^2
     */
    var ax: Float = accelerometer.x
    var ay: Float = accelerometer.y
    var az: Float = accelerometer.z

    /**
     * "g" mean gravity
     * x, y, z is values in X-axis, Y-axis, Z-axis to make a 3D vector in space
     *
     * @unit m/s^2
     */
    var gx: Float = gravity.x
    var gy: Float = gravity.y
    var gz: Float = gravity.z
}