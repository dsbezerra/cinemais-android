package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.cinemaisapp.CinemaisApplication
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        RepositoryModule::class,
        ActivityBindingModule::class,
        FragmentBindingModule::class,
        ChildFragmentBindingModule::class,
        ViewModelModule::class,
        WorkerModule::class
    ]
)
interface AppComponent : AndroidInjector<CinemaisApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<CinemaisApplication>()

}