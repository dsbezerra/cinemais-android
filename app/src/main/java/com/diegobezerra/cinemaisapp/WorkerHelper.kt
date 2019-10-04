package com.diegobezerra.cinemaisapp

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory

class WorkerHelper {

    companion object {

        @JvmStatic
        fun initialize(context: Context, factory: WorkerFactory) {
            WorkManager.initialize(
                context,
                Configuration.Builder().setWorkerFactory(factory).build()
            )
        }

    }

}