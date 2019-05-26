package com.diegobezerra.cinemaisapp.ui.tickets

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment.Companion.CINEMA_ID
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

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TicketsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_tickets, container, false)
        val content = root.findViewById<TextView>(R.id.content)
        val buyOnline = root.findViewById<Button>(R.id.buy_online)
        val progressBar = root.findViewById<View>(R.id.progress_bar)

        val args = requireNotNull(arguments)
        viewModel.apply {

            tickets.observe(this@TicketsFragment, Observer { tickets ->
                content.text = Html.fromHtml(tickets.content)
                if (tickets.buyOnlineUrl.isNotEmpty()) {
                    buyOnline.isGone = false
                    buyOnline.setOnClickListener {
                        startActivity(Intent(ACTION_VIEW, Uri.parse(tickets.buyOnlineUrl)))
                    }
                } else {
                    buyOnline.isGone = true
                }
                progressBar.isGone = true
            })

            viewModel.postCinemaId(args.getInt(CINEMA_ID))
        }

        return root
    }

}
