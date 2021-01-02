package com.albertkhang.potholedetection.util

import com.albertkhang.potholedetection.model.IVector3D

class IRIUtil {
    companion object {
        fun getIRI(accelerometer: IVector3D, gravity: IVector3D): Float {
            return accelerometer.iri(gravity)
        }
    }
}