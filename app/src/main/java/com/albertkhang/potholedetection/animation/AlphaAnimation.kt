package com.albertkhang.potholedetection.animation

import android.animation.Animator
import android.view.View

class AlphaAnimation {
    companion object {
        private const val duration = 200L

        fun showViewAnimation(view: View?, listener: Animator.AnimatorListener?) {
            if (view != null) {
                view.alpha = 0f
                view.animate().alpha(1f).setListener(listener).setDuration(duration).start()
            }
        }

        fun hideViewAnimation(view: View?, listener: Animator.AnimatorListener?) {
            if (view != null) {
                view.alpha = 1f
                view.animate().alpha(0f).setListener(listener).setDuration(duration).start()
            }
        }
    }
}