package com.diegobezerra.cinemaisapp.ui.tickets

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentTicketsBinding
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment.Companion.CINEMA_ID
import com.diegobezerra.core.event.EventObserver
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TicketsFragment : DaggerFragment() {

    companion object {

        fun newInstance(cinemaId: Int): TicketsFragment {
            val args = Bundle().apply {
                putInt(CINEMA_ID, cinemaId)
            }
            return TicketsFragment().apply {
                arguments = args
            }
        }

    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val ticketsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TicketsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentTicketsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@TicketsFragment
            viewModel = ticketsViewModel
        }

        val root = binding.root
        val progress = root.findViewById<View>(R.id.progress_bar)

        ticketsViewModel.loading.observe(this, Observer {
            progress.isGone = !it
        })

        ticketsViewModel.navigateToBuyWebsiteAction.observe(this, EventObserver { buyOnlineUrl ->
            startActivity(Intent(ACTION_VIEW, Uri.parse(buyOnlineUrl)))
        })

        return root
    }

    override fun onStart() {
        super.onStart()

        ticketsViewModel.setCinemaId(requireNotNull(arguments).getInt(CINEMA_ID))
    }

}
