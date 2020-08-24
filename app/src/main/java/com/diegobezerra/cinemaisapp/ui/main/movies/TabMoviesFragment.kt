package com.diegobezerra.cinemaisapp.ui.main.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.util.NetworkUtils
import com.diegobezerra.cinemaisapp.widget.EmptyView
import com.diegobezerra.core.event.EventObserver
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TabMoviesFragment : DaggerFragment() {

    companion object {

        const val TYPE = "arg.TYPE"

        fun newInstance(type: Int): TabMoviesFragment {
            return TabMoviesFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE, type)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TabMoviesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tab_movie, container, false)
        val swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        val emptyView = root.findViewById<EmptyView>(R.id.emptyView)
        val type = requireNotNull(arguments).getInt(TYPE)

        val moviesAdapter =
            MoviesAdapter(viewModel, type, NetworkUtils.isWifiConnection(requireActivity()))
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.run {
            adapter = moviesAdapter
            setHasFixedSize(true)
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.apply {
            loading.observe(viewLifecycleOwner, {
                swipeRefreshLayout.isRefreshing = it
            })

            movies.observe(viewLifecycleOwner, {
                moviesAdapter.submitList(it)
                emptyView.isGone = it.isNotEmpty()
            })

            navigateToMovieDetail.observe(viewLifecycleOwner, EventObserver { movieId ->
                startActivity(MovieActivity.getStartIntent(requireActivity(), movieId))
            })

            setType(type)
        }

        return root
    }

}
