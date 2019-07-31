package com.diegobezerra.cinemaisapp.ui.schedule.filters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.databinding.ItemScheduleFilterBinding
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFiltersAdapter.ScheduleFiltersViewHolder

class ScheduleFiltersAdapter(
    private val filterableSchedule: FilterableSchedule
) : ListAdapter<ScheduleFilter, ScheduleFiltersViewHolder>(FilterDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleFiltersViewHolder {
        val binding = ItemScheduleFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).apply {
            filterableSchedule = this@ScheduleFiltersAdapter.filterableSchedule
        }
        return ScheduleFiltersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleFiltersViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ScheduleFiltersViewHolder(private val binding: ItemScheduleFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal fun bind(item: ScheduleFilter) {
            binding.scheduleFilter = item
            binding.executePendingBindings()
        }

    }
}

object FilterDiff : DiffUtil.ItemCallback<ScheduleFilter>() {

    override fun areItemsTheSame(oldItem: ScheduleFilter, newItem: ScheduleFilter): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ScheduleFilter, newItem: ScheduleFilter): Boolean {
        return oldItem == newItem
    }

}

