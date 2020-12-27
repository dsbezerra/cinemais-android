package com.diegobezerra.cinemaisapp.util

import android.app.Activity
import android.content.Context

class ContextUtils {

    companion object {

        fun isDestroyed(context: Context?): Boolean {
            if (context == null) {
                return true
            }
            if (context is Activity &&
                (context.isDestroyed || context.isFinishing)
            ) {
                return true
            }
            return false
        }

    }
}