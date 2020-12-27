package com.diegobezerra.cinemaisapp.ui.main.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout.Tab
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayoutMediator
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayoutMediator.TabConfigurationStrategy

class MoviesFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.MOVIES"

        const val NOW_PLAYING = 0
        const val UPCOMING = 1

        private const val COUNT = 2

    }

    private lateinit var mediator: CinemaisTabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_movies, container, false)
        val pager = root.findViewById<ViewPager2>(R.id.viewpager)
        val tabs = root.findViewById<CinemaisTabLayout>(R.id.tabs)

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = title()
        }

        pager.adapter = MoviesAdapter(this)

        mediator = CinemaisTabLayoutMediator(tabs, pager,
            object : TabConfigurationStrategy {
                override fun onConfigureTab(tab: Tab, position: Int) {
                    tab.setText(
                        when (position) {
                            NOW_PLAYING -> R.string.title_tab_now_playing
                            else -> R.string.title_tab_upcoming
                        }
                    )
                }
            })

        mediator.attach()

        return root
    }

    override fun onDestroyView() {
        mediator.detach()

        super.onDestroyView()
    }

    override fun title(): String? = getString(R.string.title_movies)

    inner class MoviesAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = COUNT

        override fun createFragment(position: Int): Fragment {
            return TabMoviesFragment.newInstance(position)
        }
    }

}
