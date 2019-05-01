package com.diegobezerra.cinemaisapp.ui.about

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.diegobezerra.cinemaisapp.R

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

        findViewById<View>(R.id.back).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.about_app_1)
            .setLinkable(R.string.about_app_1)

        findViewById<TextView>(R.id.about_app_2)
            .setLinkable(R.string.about_app_2)
    }

    private fun TextView.setLinkable(resId: Int) {
        movementMethod = LinkMovementMethod.getInstance()
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(resId), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(getString(resId))
        }
    }
}
