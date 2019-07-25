package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.CinemaViewHolder
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.StateViewHolder
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.State

class CinemasAdapter(
    private val cinemasViewModel: CinemasViewModel
) : ListAdapter<Any, CinemasViewHolder>(CinemaDiff) {

    companion object {
        private const val VIEW_TYPE_STATE = 0
        private const val VIEW_TYPE_CINEMA = 1
    }

    var data: List<Cinema> = emptyList()
        set(value) {
            field = value
            submitList(buildList())
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
                val state = getItem(position) as State
                name.text = state.name
            }
            is CinemaViewHolder -> holder.apply {
                val cinema = getItem(position) as Cinema
                if (cinema.cityName != cinema.name) {
                    name.text = itemView.resources.getString(
                        R.string.label_cinema,
                        cinema.name,
                        cinema.cityName
                    )
                } else {
                    name.text = cinema.name
                }

                itemView.setOnClickListener {
                    cinemasViewModel.onCinemaClicked(cinema.id)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is State -> VIEW_TYPE_STATE
            is Cinema -> VIEW_TYPE_CINEMA
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    private fun buildList(): List<Any> {
        val result = mutableListOf<Any>()
        val set = hashSetOf<String>()
        data.sortedBy { it.fu }.forEach { cinema ->
            if (set.add(cinema.fu)) {
                State.buildFromFU(cinema.fu)?.let {
                    result += it
                }
            }
            result += cinema
        }
        return result
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

object CinemaDiff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        return when {
            oldItem is State && newItem is State -> oldItem.fu == newItem.fu
            oldItem is Cinema && newItem is Cinema -> oldItem.id == newItem.id
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is State && newItem is State -> {
                oldItem.fu == newItem.fu && oldItem.name == newItem.name
            }
            oldItem is Cinema && newItem is Cinema -> oldItem == newItem
            else -> true
        }
    }
}