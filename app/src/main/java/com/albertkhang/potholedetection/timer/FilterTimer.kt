package com.albertkhang.potholedetection.timer

import android.os.CountDownTimer

class FilterTimer {
    init {
        val timer: CountDownTimer = object : CountDownTimer(1000 * 60 * 60 * 24, 1000 * 60 * 60) {
            override fun onTick(millisUntilFinished: Long) {
                // Do something
            }

            override fun onFinish() {
                // Do something
            }
        }
        timer.start()
    }

    companion object {
//        fun run()
    }
}