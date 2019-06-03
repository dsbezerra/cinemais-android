package com.diegobezerra.cinemaisapp.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceFragmentCompat
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion.PREFS_NAME
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion.PREF_DARK_THEME
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.util.setupActionBar
import com.diegobezerra.cinemaisapp.util.setupTheme

class SettingsActivity : AppCompatActivity() {

    companion object {

        fun startActivity(context: Context) {
            context.run {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = PREFS_NAME
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onResume() {
            super.onResume()

            preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()

            preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            when (key) {
                PREF_DARK_THEME -> {
                    requireActivity().run {
                        TaskStackBuilder.create(this)
                            .addNextIntent(Intent(activity, MainActivity::class.java))
                            .addNextIntent(Intent(activity, SettingsActivity::class.java))
                            .startActivities()
                    }
                }

            }
        }
    }
}