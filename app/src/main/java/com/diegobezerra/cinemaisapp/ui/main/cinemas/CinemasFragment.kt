package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.shared.result.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CinemasFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.CINEMAS"

    }

    private val cinemasViewModel: CinemasViewModel by viewModels()
    private val cinemasAdapter by lazy { CinemasAdapter(cinemasViewModel) }

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

        cinemasViewModel.apply {
            loading.observe(viewLifecycleOwner) {
                progressBar.isGone = !it
            }

            cinemas.observe(viewLifecycleOwner) {
                cinemasAdapter.data = it
            }

            switchToCinemaDetail.observe(viewLifecycleOwner,
                EventObserver {
                    (requireActivity() as MainActivity).run {
                        showCinemaFragment(it)
                    }
                })
        }

        return root
    }

    override fun title() = getString(R.string.title_theaters)
}
