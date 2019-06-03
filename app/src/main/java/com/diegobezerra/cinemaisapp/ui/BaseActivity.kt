package com.diegobezerra.cinemaisapp.ui

import android.os.Bundle
import com.diegobezerra.cinemaisapp.util.setupTheme
import dagger.android.support.DaggerAppCompatActivity

abstract class BaseActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
    }
}