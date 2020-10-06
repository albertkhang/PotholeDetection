package com.albertkhang.potholedetection.activity

import android.app.Application
import com.albertkhang.potholedetection.util.DatabaseUtil
import io.paperdb.Paper

public class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        DatabaseUtil.init()
    }
}