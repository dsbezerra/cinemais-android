package com.diegobezerra.cinemaisapp.ui.tickets

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentTicketsBinding
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment.Companion.CINEMA_ID
import com.diegobezerra.shared.result.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TicketsFragment : Fragment() {

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

    private val ticketsViewModel: TicketsViewModel by viewModels()
    private val ticketsAdapter by lazy { TicketAdapter(requireContext()) }

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

        binding.recyclerView.run {
            adapter = ticketsAdapter
            setHasFixedSize(true)
        }

        ticketsViewModel.loading.observe(viewLifecycleOwner) {
            progress.isGone = !it
        }

        ticketsViewModel.tickets.observe(viewLifecycleOwner) {
            ticketsAdapter.data = it.tickets
        }

        ticketsViewModel.navigateToBuyWebsiteAction.observe(
            viewLifecycleOwner,
            EventObserver { buyOnlineUrl ->
                startActivity(Intent(ACTION_VIEW, Uri.parse(buyOnlineUrl)))
            })

        return root
    }

    override fun onStart() {
        super.onStart()

        ticketsViewModel.setCinemaId(requireNotNull(arguments).getInt(CINEMA_ID))
    }

}
