package com.diegobezerra.cinemaisapp.ui.main.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.R.string
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout

class MoviesFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.MOVIES"

        const val NOW_PLAYING = 0
        const val UPCOMING = 1

        private const val COUNT = 2

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_movies, container, false)
        val pager = root.findViewById<ViewPager>(R.id.viewpager)

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = title()
        }

        pager.adapter = MoviesAdapter(childFragmentManager)

        root.findViewById<CinemaisTabLayout>(R.id.tabs).run {
            setupWithViewPager(pager)
        }
        return root
    }

    override fun title(): String? = getString(R.string.title_movies)

    inner class MoviesAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

        override fun getCount() = COUNT

        override fun getItem(position: Int): Fragment {
            return TabMoviesFragment.newInstance(position)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                NOW_PLAYING -> getString(string.title_tab_now_playing)
                else -> getString(string.title_tab_upcoming)
            }
        }
    }

}
