package com.diegobezerra.cinemaisapp.ui.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.core.cinemais.domain.model.Banner

class BannersAdapter(
    private val banners: List<Banner>
) : PagerAdapter() {

    private var listener: ((Banner) -> Unit)? = null

    override fun instantiateItem(
        container: ViewGroup,
        position: Int
    ): Any {
        val inflater = LayoutInflater.from(container.context)
        val banner =
            inflater.inflate(R.layout.layout_banner, container, false) as ImageView
        container.addView(banner)

        val b = banners[position]
        if (b.imageUrl.isNotEmpty()) {
            GlideApp.with(container.context)
                .load(b.imageUrl)
                .placeholder(ImageUtils.getPlaceholder(container.context))
                .into(banner)
        }

        listener?.let { function ->
            banner.setOnClickListener {
                function(b)
            }
        }

        return banner
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        view: Any
    ) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean = view == `object`

    override fun getCount(): Int = banners.size

    fun setListener(listener: (Banner) -> Unit) {
        this.listener = listener
    }
}