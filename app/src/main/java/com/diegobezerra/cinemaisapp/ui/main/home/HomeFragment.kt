package com.diegobezerra.cinemaisapp.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentHomeBinding
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.ui.upcoming.UpcomingMoviesActivity
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.shared.result.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import fr.castorflex.android.circularprogressbar.CircularProgressBar

@AndroidEntryPoint
class HomeFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.HOME"

    }

    private val homeViewModel: HomeViewModel by viewModels()

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

        homeViewModel.loading.observe(viewLifecycleOwner) {
            progressBar.isGone = !it
        }

        homeViewModel.home.observe(viewLifecycleOwner) {
            homeAdapter.data = it
        }

        homeViewModel.navigateToMovieDetail.observe(viewLifecycleOwner,
            EventObserver { movieId ->
                startActivity(MovieActivity.getStartIntent(requireActivity(), movieId))
            })

        homeViewModel.navigateToAllUpcomingMovies.observe(viewLifecycleOwner,
            EventObserver {
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
