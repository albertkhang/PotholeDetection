package com.albertkhang.potholedetection.model.entry

/**
 * @Type Local Database Entry
 *
 * @Description be used to contain the local data when handling
 */
abstract class LocalEntry {
    val timestamp = System.currentTimeMillis()
    abstract override fun toString(): String
}