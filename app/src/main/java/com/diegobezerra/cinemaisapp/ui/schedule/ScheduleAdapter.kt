package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat

private val DATE_TITLE_NO_WEEKDAY = SimpleDateFormat("d 'de' MMM", BRAZIL)
private val DATE_TITLE_WEEKDAY = SimpleDateFormat("E', 'd 'de' MMM", BRAZIL)

class ScheduleAdapter(
    val fragment: Fragment,
    val schedule: Schedule,
    private val playingRooms: Boolean = false
) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment =
        ScheduleDayFragment.newInstance(schedule.cinema.id, position, playingRooms)

    fun getPageTitle(position: Int): CharSequence? {
        val day = schedule.days[position].day
        return when {
            DateUtils.isToday(day.time) -> {
                fragment.getString(R.string.today_with_date, DATE_TITLE_NO_WEEKDAY.format(day))
            }
            DateUtils.isTomorrow(day.time) -> {
                fragment.getString(R.string.tomorrow_with_date, DATE_TITLE_NO_WEEKDAY.format(day))
            }
            else -> DATE_TITLE_WEEKDAY.format(day.time)
        }
    }

    override fun getItemCount(): Int = schedule.days.size

}