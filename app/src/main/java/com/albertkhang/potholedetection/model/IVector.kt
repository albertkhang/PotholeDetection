package com.albertkhang.potholedetection.model

class IVector3D(var x: Float, var y: Float, var z: Float) {

    fun project(v: IVector3D): Float {
        return this.dot(v) / v.dot(v)
    }

    private fun dot(v: IVector3D): Float {
        return x * v.x + y * v.y + z * v.z
    }
}