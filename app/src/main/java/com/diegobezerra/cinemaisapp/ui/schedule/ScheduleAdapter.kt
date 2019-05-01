package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.diegobezerra.core.cinemais.domain.model.Schedule
import java.text.SimpleDateFormat

private val DATE_TITLE_FORMAT = SimpleDateFormat("E'\n'd MMM")

class ScheduleAdapter(
    var schedule: Schedule,
    fm: FragmentManager,
    val light: Boolean = true
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return ScheduleDayFragment.newInstance(schedule.cinema.id, position, light)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val day = schedule.days[position].day
        return DATE_TITLE_FORMAT.format(day.time)
    }

    override fun getCount() = schedule.days.size

}