package com.diegobezerra.cinemaisapp.ui.schedule.filters

import androidx.databinding.ObservableBoolean
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.core.cinemais.domain.model.Session

class ScheduleFilter(
    val id: String, // Matches session values
    val labelRes: Int,
    isChecked: Boolean
) {

    companion object {

        fun createFilter(id: String, isChecked: Boolean): ScheduleFilter {
            val label = when (id) {
                Session.VersionDubbed -> R.string.filter_audio_dub
                Session.VersionSubtitled -> R.string.filter_audio_sub
                Session.VersionNational -> R.string.filter_audio_nac
                Session.VideoFormat2D -> R.string.filter_video_2d
                Session.VideoFormat3D -> R.string.filter_video_3d
                Session.RoomMagicD -> R.string.filter_room_magicd
                Session.RoomVIP -> R.string.filter_room_vip
                else -> throw IllegalStateException("invalid filter $id")
            }
            return ScheduleFilter(id, label, isChecked)
        }

    }

    val isChecked = ObservableBoolean(isChecked)
}