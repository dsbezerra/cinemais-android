package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat

private val DATE_TITLE_FORMAT = SimpleDateFormat("E', 'd MMM", BRAZIL)

class ScheduleAdapter(
    private val titles: List<String>,
    var schedule: Schedule,
    fm: FragmentManager,
    val light: Boolean = true
) : FragmentStatePagerAdapter(fm) {

    companion object {

        const val POSITION_TODAY = 0
        const val POSITION_TOMORROW = 1

    }

    init {
        if (titles.size < 2) {
            throw IllegalStateException("schedule adapter requires title list")
        }
    }

    override fun getItem(position: Int): Fragment {
        return ScheduleDayFragment.newInstance(schedule.cinema.id, position, light)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val day = schedule.days[position].day
        if (DateUtils.isToday(day.time)) {
            return titles[POSITION_TODAY]
        } else if (DateUtils.isTomorrow(day.time)) {
            return titles[POSITION_TOMORROW]
        }
        return DATE_TITLE_FORMAT.format(day.time)
    }

    override fun getCount() = schedule.days.size

}