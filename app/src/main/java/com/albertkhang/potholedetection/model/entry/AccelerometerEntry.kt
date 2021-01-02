package com.albertkhang.potholedetection.model.entry

/**
 * @Type Local Database Entry
 *
 * @Description be used to contain the accelerometer data when handling
 */
data class AccelerometerEntry(val iri: Float) : LocalEntry() {
    override fun toString(): String {
        return "AccelerometerEntry(timestamp=${timestamp}, iri=$iri)"
    }
}