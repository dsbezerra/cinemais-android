package com.diegobezerra.cinemaisapp.ui.schedule

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat

private val DATE_TITLE_NO_WEEKDAY = SimpleDateFormat("d 'de' MMM", BRAZIL)
private val DATE_TITLE_WEEKDAY = SimpleDateFormat("E', 'd 'de' MMM", BRAZIL)

class ScheduleAdapter(
    fm: FragmentManager,
    val context: Context,
    val schedule: Schedule,
    val playingRooms: Boolean = false
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int) =
        ScheduleDayFragment.newInstance(schedule.cinema.id, position, playingRooms)

    override fun getPageTitle(position: Int): CharSequence? {
        val day = schedule.days[position].day
        return when {
            DateUtils.isToday(day.time) -> {
                context.getString(R.string.today_with_date, DATE_TITLE_NO_WEEKDAY.format(day))
            }
            DateUtils.isTomorrow(day.time) -> {
                context.getString(R.string.tomorrow_with_date, DATE_TITLE_NO_WEEKDAY.format(day))
            }
            else -> DATE_TITLE_WEEKDAY.format(day.time)
        }
    }

    override fun getCount() = schedule.days.size

}