package com.albertkhang.potholedetection.util

import android.content.Context

class DisplayUtil {
    companion object {
        fun getDensity(context: Context): Float {
            return context.resources.displayMetrics.density
        }
    }
}