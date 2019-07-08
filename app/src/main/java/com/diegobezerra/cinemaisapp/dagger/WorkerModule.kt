package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.cinemaisapp.tasks.CheckPremieresWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(CheckPremieresWorker::class)
    abstract fun bindCheckPremieresWorker(factory: CheckPremieresWorker.Factory):
        DaggerWorkerFactory.ChildWorkerFactory

}