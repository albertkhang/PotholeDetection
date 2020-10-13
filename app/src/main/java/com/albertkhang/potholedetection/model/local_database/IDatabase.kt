package com.albertkhang.potholedetection.model.local_database

abstract class IDatabase {
//    var userId: Int = userId

    /**
     * Time and data are collected
     *
     * @unit milliseconds
     */
    var timestamps: Long = 0

    abstract override fun toString(): String
}