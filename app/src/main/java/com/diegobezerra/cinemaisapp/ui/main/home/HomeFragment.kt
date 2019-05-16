package com.diegobezerra.cinemaisapp.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.ui.upcoming.UpcomingMoviesActivity
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.core.event.EventObserver
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import javax.inject.Inject

class HomeFragment : MainFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)
    }

    private val homeAdapter by lazy { HomeAdapter(viewModel) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = root.findViewById<CircularProgressBar>(R.id.progress_bar)

        homeAdapter.restore(savedInstanceState)
        recyclerView.run {
            adapter = homeAdapter
            setHasFixedSize(true)
        }

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = title()
        }

        viewModel.apply {
            loading.observe(this@HomeFragment, Observer {
                progressBar.isGone = !it
            })

            home.observe(this@HomeFragment, Observer {
                homeAdapter.data = it
                homeAdapter.notifyDataSetChanged()
            })

            navigateToMovieDetail.observe(this@HomeFragment, EventObserver { movieId ->
                startActivity(MovieActivity.getStartIntent(requireActivity(), movieId))
            })

            navigateToAllUpcomingMovies.observe(this@HomeFragment, EventObserver {
                startActivity(UpcomingMoviesActivity.getStartIntent(requireActivity()))
            })
        }

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        homeAdapter.save(outState)
    }

    override fun id() = R.id.home

    override fun title() = getString(R.string.title_home)
}
