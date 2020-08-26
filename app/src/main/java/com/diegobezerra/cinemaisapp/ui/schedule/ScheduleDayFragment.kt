package com.diegobezerra.cinemaisapp.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.databinding.FragmentScheduleDayBinding
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaViewModel
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel
import com.diegobezerra.cinemaisapp.util.parentFragmentViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleDayFragment : Fragment() {

    companion object {

        const val CINEMA_ID = "arg.CINEMA_ID"
        const val DAY_POSITION = "arg.DAY_POSITION"

        // Whether the fragment was created inside a PlayingRoomsFragment or not.
        const val PLAYING_ROOMS = "arg.PLAYING_ROOMS"

        fun newInstance(cinema: Int, day: Int, playingRooms: Boolean = false): ScheduleDayFragment {
            return ScheduleDayFragment().apply {
                arguments = Bundle().apply {
                    putInt(CINEMA_ID, cinema)
                    putInt(DAY_POSITION, day)
                    putBoolean(PLAYING_ROOMS, playingRooms)
                }
            }
        }
    }

    private val playingCinemasViewModel: PlayingCinemasViewModel by parentFragmentViewModels()
    private val cinemaViewModel: CinemaViewModel by parentFragmentViewModels()

    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentScheduleDayBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@ScheduleDayFragment
        }

        val args = requireNotNull(arguments)

        val playingRooms = args.getBoolean(PLAYING_ROOMS)
        val dayPosition = args.getInt(DAY_POSITION)
        binding.recyclerView.run {
            adapter = SessionsAdapter(playingRooms).also {
                sessionsAdapter = it
            }
            setHasFixedSize(true)
        }

        // @Temporary
        val schedule =
            if (playingRooms) playingCinemasViewModel.schedule else cinemaViewModel.schedule
        schedule.observe(viewLifecycleOwner, {
            it.getDay(dayPosition)?.let { day ->
                sessionsAdapter.data = day.sessions
                sessionsAdapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }
}
