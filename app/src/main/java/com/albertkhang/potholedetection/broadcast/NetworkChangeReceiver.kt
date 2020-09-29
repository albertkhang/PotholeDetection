package com.albertkhang.potholedetection.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.albertkhang.potholedetection.util.NetworkUtil

class NetworkChangeReceiver : BroadcastReceiver() {
    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }


    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

    interface OnNetworkChangeListener {
        fun onNetworkOn()
        fun onNetworkOff()
    }

    private var networkListener: OnNetworkChangeListener? = null

    fun setOnNetworkChangeListener(networkListener: OnNetworkChangeListener) {
        this.networkListener = networkListener
    }

    override fun onReceive(context: Context?, p1: Intent?) {
        if (context != null) {
            if (networkListener != null && NetworkUtil.isNetworkAvailable(context)) {
                networkListener!!.onNetworkOn()
            } else {
                networkListener!!.onNetworkOff()
            }
        }
    }


}