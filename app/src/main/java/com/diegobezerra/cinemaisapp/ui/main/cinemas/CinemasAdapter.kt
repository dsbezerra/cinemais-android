package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.ItemCinemaBinding
import com.diegobezerra.cinemaisapp.databinding.ItemStateBinding
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.CinemaViewHolder
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewHolder.StateViewHolder
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.State

class CinemasAdapter(
    private val eventListener: CinemasEventListener
) : ListAdapter<Any, CinemasViewHolder>(CinemasDiff) {

    var data: List<Cinema> = emptyList()
        set(value) {
            field = value
            submitList(buildList())
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CinemasViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_state -> StateViewHolder(
                ItemStateBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            R.layout.item_cinema -> CinemaViewHolder(
                ItemCinemaBinding.inflate(
                    inflater,
                    parent,
                    false
                ),
                eventListener
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: CinemasViewHolder, position: Int) {
        when (holder) {
            is StateViewHolder -> holder.bind(getItem(position) as State)
            is CinemaViewHolder -> holder.bind(getItem(position) as Cinema)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is State -> R.layout.item_state
            is Cinema -> R.layout.item_cinema
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
        private val binding: ItemStateBinding
    ) : CinemasViewHolder(binding.root) {
        fun bind(state: State) {
            binding.sstate = state
            binding.executePendingBindings()
        }
    }

    class CinemaViewHolder(
        private val binding: ItemCinemaBinding,
        private val eventListener: CinemasEventListener,
    ) : CinemasViewHolder(binding.root) {
        fun bind(cinema: Cinema) {
            binding.eventListener = eventListener
            binding.cinema = cinema
            binding.executePendingBindings()
        }
    }
}

object CinemasDiff : DiffUtil.ItemCallback<Any>() {
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