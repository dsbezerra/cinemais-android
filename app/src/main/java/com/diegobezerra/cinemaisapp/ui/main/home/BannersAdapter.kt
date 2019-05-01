package com.diegobezerra.cinemaisapp.ui.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
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
        if (!b.imageUrl.isEmpty()) {
            GlideApp.with(container.context)
                .asBitmap()
                .load(b.imageUrl)
                .placeholder(ImageUtils.placeholder(container.context))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(BitmapTransitionOptions.withCrossFade())
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