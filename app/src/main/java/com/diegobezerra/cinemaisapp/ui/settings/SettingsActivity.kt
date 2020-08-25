package com.diegobezerra.cinemaisapp.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion.PREFS_NAME
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion.PREF_DARK_THEME
import com.diegobezerra.cinemaisapp.util.setFragment
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        setFragment(R.id.settings) {
            SettingsFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                        (this as AppCompatActivity).setupTheme()
                    }
                }

            }
        }
    }
}