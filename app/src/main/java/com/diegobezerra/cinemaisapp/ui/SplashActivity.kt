package com.diegobezerra.cinemaisapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.util.setupTheme

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
