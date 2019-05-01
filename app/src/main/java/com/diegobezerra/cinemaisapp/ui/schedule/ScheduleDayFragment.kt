package com.diegobezerra.cinemaisapp.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ScheduleDayFragment : DaggerFragment() {

    companion object {

        const val LIGHT = "arg.LIGHT"
        const val CINEMA_ID = "arg.CINEMA_ID"
        const val DAY_POSITION = "arg.DAY_POSITION"

        fun newInstance(cinema: Int, day: Int, light: Boolean = true): ScheduleDayFragment {
            return ScheduleDayFragment().apply {
                arguments = Bundle().apply {
                    putInt(CINEMA_ID, cinema)
                    putInt(DAY_POSITION, day)
                    putBoolean(LIGHT, light)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(parentFragment!!, viewModelFactory)
            .get(ScheduleViewModel::class.java)
    }

    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = requireNotNull(arguments)
        val root = inflater.inflate(R.layout.fragment_schedule_day, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.run {
            adapter = SessionsAdapter(args.getBoolean(LIGHT)).also {
                sessionsAdapter = it
            }
            setHasFixedSize(true)
        }
        viewModel.apply {

            val cinemaId = args.getInt(CINEMA_ID)
            val dayPosition = args.getInt(DAY_POSITION)

            schedule.observe(this@ScheduleDayFragment, Observer {
                getScheduleForDay(dayPosition)?.observe(
                    this@ScheduleDayFragment,
                    Observer {
                        sessionsAdapter.data = it.sessions
                        sessionsAdapter.notifyDataSetChanged()
                    })
            })

            setCinema(cinemaId)
        }

        return root
    }

}
