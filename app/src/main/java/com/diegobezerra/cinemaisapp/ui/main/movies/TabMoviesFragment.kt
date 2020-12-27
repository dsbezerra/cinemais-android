package com.diegobezerra.cinemaisapp.ui.main.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.util.NetworkUtils
import com.diegobezerra.cinemaisapp.widget.EmptyView
import com.diegobezerra.shared.result.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabMoviesFragment : Fragment() {

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

    private val tabMoviesViewModel: TabMoviesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tab_movie, container, false)
        val swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        val emptyView = root.findViewById<EmptyView>(R.id.emptyView)
        val type = requireNotNull(arguments).getInt(TYPE)

        val moviesAdapter =
            MoviesAdapter(
                tabMoviesViewModel,
                type,
                NetworkUtils.isWifiConnection(requireActivity())
            )
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.run {
            adapter = moviesAdapter
            setHasFixedSize(true)
        }

        swipeRefreshLayout.setOnRefreshListener {
            tabMoviesViewModel.refresh()
        }

        tabMoviesViewModel.apply {
            loading.observe(viewLifecycleOwner) {
                swipeRefreshLayout.isRefreshing = it
            }

            movies.observe(viewLifecycleOwner) {
                moviesAdapter.submitList(it)
                emptyView.isGone = it.isNotEmpty()
            }

            navigateToMovieDetail.observe(viewLifecycleOwner,
                EventObserver { movieId ->
                    startActivity(MovieActivity.getStartIntent(requireActivity(), movieId))
                })

            setType(type)
        }

        return root
    }

}
