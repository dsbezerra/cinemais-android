package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.CinemaViewHolder
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.StateViewHolder
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.State

class CinemasAdapter(
    private val cinemasViewModel: CinemasViewModel
) : RecyclerView.Adapter<CinemasViewHolder>() {

    companion object {
        private const val VIEW_TYPE_STATE = 0
        private const val VIEW_TYPE_CINEMA = 1
    }

    private var list: MutableList<Any> = mutableListOf()

    var data: List<Cinema> = emptyList()
        set(value) {
            field = value
            buildList()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CinemasViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_STATE -> StateViewHolder(
                inflater.inflate(R.layout.item_cinema_state, parent, false)
            )
            VIEW_TYPE_CINEMA -> CinemaViewHolder(
                inflater.inflate(R.layout.item_cinema, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: CinemasViewHolder, position: Int) {
        when (holder) {
            is StateViewHolder -> holder.apply {
                val state = list[position] as State
                name.text = state.name
            }
            is CinemaViewHolder -> holder.apply {
                val cinema = list[position] as Cinema

                if (cinema.cityName != cinema.name) {
                    name.text = "${cinema.name} - ${cinema.cityName}"
                } else {
                    name.text = "${cinema.name}"
                }

                itemView.setOnClickListener {
                    cinemasViewModel.onCinemaClicked(cinema.id)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is State -> VIEW_TYPE_STATE
            is Cinema -> VIEW_TYPE_CINEMA
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun getItemCount() = list.size

    private fun buildList() {
        list.clear()

        if (data.isEmpty()) return

        val result = mutableListOf<Any>()

        // Used just to keep track of inserted states
        val stateMap = hashMapOf<String, Unit>()
        data.sortedBy { it.federativeUnit }.forEach { cinema ->
            if (!stateMap.containsKey(cinema.federativeUnit)) {
                val state = State.buildFromFederativeUnit(cinema.federativeUnit)
                state?.let {
                    result += it
                    stateMap.put(it.federativeUnit, Unit)
                }
            }
            result += cinema
        }

        list = result
    }
}

sealed class CinemasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class StateViewHolder(
        itemView: View
    ) : CinemasViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
    }

    class CinemaViewHolder(
        itemView: View
    ) : CinemasViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.name)
    }
}