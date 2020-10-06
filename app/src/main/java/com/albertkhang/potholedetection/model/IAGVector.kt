package com.albertkhang.potholedetection.model

class IAGVector : IDatabase() {
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
}