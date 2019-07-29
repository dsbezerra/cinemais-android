package com.diegobezerra.cinemaisapp.ui.schedule.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.diegobezerra.cinemaisapp.databinding.FragmentScheduleFiltersBinding
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaViewModel
import com.diegobezerra.cinemaisapp.widget.ScheduleFilterView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ScheduleFiltersFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val cinemaViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(CinemaViewModel::class.java)
    }

    private val scheduleFiltersAdapter by lazy { ScheduleFiltersAdapter(cinemaViewModel) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentScheduleFiltersBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@ScheduleFiltersFragment
            viewModel = cinemaViewModel
        }
        binding.recyclerView.apply {
            adapter = scheduleFiltersAdapter
            setHasFixedSize(true)
        }
        return binding.root
    }
}

@BindingAdapter("scheduleFilter", "viewModel", requireAll = true)
fun setClickListenerForFilter(
    filter: ScheduleFilterView,
    scheduleFilter: ScheduleFilter,
    viewModel: CinemaViewModel
) {
    filter.setOnClickListener {
        val checked = !filter.isChecked
        filter.animateCheckedAndInvoke(checked) {
            viewModel.toggleFilter(scheduleFilter, checked)
        }
    }
}