package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.core.event.EventObserver
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber
import javax.inject.Inject

class CinemasFragment : MainFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(CinemasViewModel::class.java)
    }

    private val cinemasAdapter by lazy { CinemasAdapter(viewModel) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cinemas, container, false)
        val progressBar = root.findViewById<View>(R.id.progress_bar)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.run {
            adapter = cinemasAdapter
            setHasFixedSize(true)
        }

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = title()
        }

        viewModel.apply {
            loading.observe(this@CinemasFragment, Observer {
                progressBar.isGone = !it
            })

            cinemas.observe(this@CinemasFragment, Observer {
                cinemasAdapter.data = it
                cinemasAdapter.notifyDataSetChanged()
            })

            switchToCinemaDetail.observe(this@CinemasFragment, EventObserver { cinemaId ->
                (requireActivity() as MainActivity).run {
                    switchToCinemaFragment(cinemaId)
                }
            })
        }

        return root
    }

    override fun title() = getString(R.string.title_theaters)

}
