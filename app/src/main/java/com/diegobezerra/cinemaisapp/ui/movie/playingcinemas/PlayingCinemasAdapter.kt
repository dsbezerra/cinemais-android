package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasAdapter.PlayingCinemasViewHolder
import com.diegobezerra.core.cinemais.domain.model.Cinema

class PlayingCinemasAdapter(
    private val viewModel: PlayingCinemasViewModel
) : RecyclerView.Adapter<PlayingCinemasViewHolder>() {

    var data: List<Cinema> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayingCinemasViewHolder =
        PlayingCinemasViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_playing_cinema, parent, false)
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(
        holder: PlayingCinemasViewHolder,
        position: Int
    ) {
        holder.apply {
            val res = itemView.resources
            val cinema = data[position]
            name.text = cinema.name
            state.text = res.getString(R.string.label_cinema, cinema.cityName, cinema.fu)
            itemView.apply {
                setOnClickListener {
                    viewModel.onCinemaClicked(cinema.id)
                }
            }
        }
    }

    class PlayingCinemasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.name)
        val state: TextView = itemView.findViewById(R.id.state)

    }
}