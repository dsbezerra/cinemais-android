package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentCinemasBinding
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
        val binding = FragmentCinemasBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@CinemasFragment
            viewModel = cinemasViewModel
        }
        setupToolbarAsActionBar(binding.root, R.id.toolbar) {
            title = title()
        }

        binding.recyclerView.run {
            adapter = cinemasAdapter
            setHasFixedSize(true)
        }

        cinemasViewModel.cinemas.observe(viewLifecycleOwner) {
            cinemasAdapter.data = it
        }

        cinemasViewModel.switchToCinemaDetail.observe(viewLifecycleOwner,
            EventObserver {
                mainViewModel.onShowCinema(it)
            })

        return binding.root
    }

    override fun title() = getString(R.string.title_theaters)
}
