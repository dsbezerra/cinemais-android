package com.diegobezerra.cinemaisapp.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentHomeBinding
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.ui.upcoming.UpcomingMoviesActivity
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.core.event.EventObserver
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import javax.inject.Inject

class HomeFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.HOME"

    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)
    }

    private val homeAdapter by lazy { HomeAdapter(homeViewModel) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@HomeFragment
            viewModel = homeViewModel
        }

        val root = binding.root
        val progressBar = root.findViewById<CircularProgressBar>(R.id.progress_bar)

        homeAdapter.restore(savedInstanceState)
        binding.recyclerView.run {
            adapter = homeAdapter
            setHasFixedSize(true)
        }

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = title()
        }

        homeViewModel.loading.observe(this@HomeFragment, Observer {
            progressBar.isGone = !it
        })

        homeViewModel.home.observe(this@HomeFragment, Observer {
            homeAdapter.data = it
        })

        homeViewModel.navigateToMovieDetail.observe(this@HomeFragment, EventObserver { movieId ->
            startActivity(MovieActivity.getStartIntent(requireActivity(), movieId))
        })

        homeViewModel.navigateToAllUpcomingMovies.observe(this@HomeFragment, EventObserver {
            startActivity(UpcomingMoviesActivity.getStartIntent(requireActivity()))
        })

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        homeAdapter.save(outState)
    }

    override fun id() = R.id.home

    override fun title() = getString(R.string.title_home)
}
