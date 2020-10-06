package com.albertkhang.potholedetection.activity

import android.app.Application
import com.albertkhang.potholedetection.util.LocalDatabaseUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import io.paperdb.Paper

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        LocalDatabaseUtil.init()
        FirebaseFirestore.getInstance()
    }
}