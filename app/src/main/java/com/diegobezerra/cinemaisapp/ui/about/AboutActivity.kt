package com.diegobezerra.cinemaisapp.ui.about

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.setupActionBar
import com.diegobezerra.cinemaisapp.util.setupTheme

class AboutActivity : AppCompatActivity() {

    companion object {

        fun startActivity(context: Context) {
            context.run {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        findViewById<TextView>(R.id.about_app_2)
            .setLinkable(R.string.about_app_2)

        findViewById<TextView>(R.id.about_app_3)
            .setLinkable(R.string.about_app_3)
    }

    private fun TextView.setLinkable(resId: Int) {
        movementMethod = LinkMovementMethod.getInstance()
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(resId), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(getString(resId))
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
}
