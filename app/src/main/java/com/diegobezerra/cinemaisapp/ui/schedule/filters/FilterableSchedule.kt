package com.diegobezerra.cinemaisapp.ui.schedule.filters

import androidx.databinding.BindingAdapter
import com.diegobezerra.cinemaisapp.widget.ScheduleFilterView

interface FilterableSchedule {

    fun onToggleFilter(filter: ScheduleFilter, checked: Boolean)

}

@BindingAdapter("scheduleFilter", "filterableSchedule", requireAll = true)
fun setClickListenerForFilter(
    filter: ScheduleFilterView,
    scheduleFilter: ScheduleFilter,
    filterable: FilterableSchedule
) {
    filter.setOnClickListener {
        val checked = !filter.isChecked
        filter.animateCheckedAndInvoke(checked) {
            filterable.onToggleFilter(scheduleFilter, checked)
        }
    }
}