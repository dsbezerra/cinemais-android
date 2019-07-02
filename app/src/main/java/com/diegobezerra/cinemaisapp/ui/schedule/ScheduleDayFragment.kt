package com.diegobezerra.cinemaisapp.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.diegobezerra.cinemaisapp.databinding.FragmentScheduleDayBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ScheduleDayFragment : DaggerFragment() {

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

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val scheduleViewModel by lazy {
        ViewModelProviders.of(parentFragment!!, viewModelFactory)
            .get(ScheduleViewModel::class.java)
    }

    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentScheduleDayBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@ScheduleDayFragment
            viewModel = scheduleViewModel
        }

        val args = requireNotNull(arguments)

        binding.recyclerView.run {
            val playingRooms = args.getBoolean(PLAYING_ROOMS)
            adapter = SessionsAdapter(playingRooms).also {
                sessionsAdapter = it
            }
            setHasFixedSize(true)
        }

        scheduleViewModel.apply {
            val dayPosition = args.getInt(DAY_POSITION)
            schedule.observe(this@ScheduleDayFragment, Observer {
                it.getDay(dayPosition)?.let { day ->
                    sessionsAdapter.data = day.sessions
                    sessionsAdapter.notifyDataSetChanged()
                }
            })
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        scheduleViewModel.setCinemaId(requireNotNull(arguments).getInt(CINEMA_ID))
    }
}
