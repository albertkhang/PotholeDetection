package com.albertkhang.potholedetection.activity

import android.app.Application
import io.paperdb.Paper

public class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }
}