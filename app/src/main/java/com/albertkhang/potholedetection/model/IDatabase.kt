package com.albertkhang.potholedetection.model

abstract class IDatabase {
//    var userId: Int = userId

    /**
     * Time and data are collected
     *
     * @unit milliseconds
     */
    var timestamps: Long = System.currentTimeMillis()
}