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

    interface OnNetworkOnOffListener {
        fun onNetworkOn()
        fun onNetworkOff()
    }

    interface OnNetworkChangeListener {
        fun onNetworkChangeListener(context: Context)
    }

    private var networkListener: OnNetworkOnOffListener? = null
    private var onNetworkChangeListener: OnNetworkChangeListener? = null

    fun setOnNetworkChangeListener(networkListener: OnNetworkOnOffListener) {
        this.networkListener = networkListener
    }

    fun setOnNetworkChangeListener(onNetworkChangeListener: OnNetworkChangeListener) {
        this.onNetworkChangeListener = onNetworkChangeListener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (networkListener != null && NetworkUtil.isNetworkAvailable(context)) {
                networkListener!!.onNetworkOn()
            } else {
                networkListener!!.onNetworkOff()
            }

            if (onNetworkChangeListener != null) {
                onNetworkChangeListener!!.onNetworkChangeListener(context)
            }
        }
    }


}