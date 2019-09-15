package com.diegobezerra.cinemaisapp.ui.tickets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsViewHolder.HeaderViewHolder
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsViewHolder.TickerViewHolder
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsViewHolder.WeekdaysViewHolder
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat2D
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat3D
import com.diegobezerra.core.cinemais.domain.model.Ticket
import com.diegobezerra.core.cinemais.domain.model.Weekdays
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL

class TicketAdapter : RecyclerView.Adapter<TicketsViewHolder>() {

    companion object {

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_WEEKDAYS = 1
        private const val VIEW_TYPE_TICKET = 2

    }

    var data: List<Ticket> = emptyList()
        set(value) {
            field = value
            buildList()
            notifyDataSetChanged()
        }

    val list = mutableListOf<Any>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TicketsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_ticket_header, parent, false) as TextView
            )
            VIEW_TYPE_WEEKDAYS -> WeekdaysViewHolder(
                inflater.inflate(R.layout.item_ticket_weekdays, parent, false)
            )
            VIEW_TYPE_TICKET -> TickerViewHolder(
                inflater.inflate(R.layout.item_ticket, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: TicketsViewHolder,
        position: Int
    ) {
        when (holder) {

            is HeaderViewHolder -> {
                val view = (holder.itemView as TextView)
                when (getItem(position)) {
                    is NormalRoomsHeader -> {
                        view.text = view.resources.getString(R.string.ticket_header_normal_rooms)
                    }
                    is MagicDHeader -> {
                        view.text = view.resources.getString(R.string.ticket_header_magic_d)
                    }
                    is MagicDVipHeader -> {
                        view.text = view.resources.getString(R.string.ticket_header_magic_d_vip)
                    }
                }
            }

            is WeekdaysViewHolder -> holder.bind(getItem(position) as Weekdays)

            is TickerViewHolder -> holder.bind(getItem(position) as Ticket)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NormalRoomsHeader, MagicDHeader, MagicDVipHeader -> VIEW_TYPE_HEADER
            is Weekdays -> VIEW_TYPE_WEEKDAYS
            is Ticket -> VIEW_TYPE_TICKET
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    private fun getItem(position: Int) = list[position]

    override fun getItemCount(): Int = list.size

    private fun buildList() {
        list.clear()

        val result = mutableListOf<Any>()
        var lastHeader: Any? = null
        var lastWeekdays: Weekdays? = null
        sort(data).forEach {
            val newHeader = getHeaderForTicket(it)
            if (lastHeader != newHeader) {
                lastHeader = newHeader
                result += newHeader
            }
            if (lastWeekdays != it.weekdays) {
                lastWeekdays = it.weekdays
                result += it.weekdays!!
            }
            result += it
        }

        list.addAll(result)
    }

    private fun sort(tickets: List<Ticket>): List<Ticket> {
        return tickets.sortedBy {
            if (it.magic && it.vip) {
                3 // Should be the last one
            } else if (it.magic) {
                2
            } else {
                1
            }
        }
    }

    private fun getHeaderForTicket(ticket: Ticket): Any {
        return if (ticket.magic && ticket.vip) {
            MagicDVipHeader
        } else if (ticket.magic) {
            MagicDHeader
        } else {
            NormalRoomsHeader
        }
    }
}

object NormalRoomsHeader

object MagicDHeader

object MagicDVipHeader

sealed class TicketsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class HeaderViewHolder(itemView: TextView) : TicketsViewHolder(itemView)

    class WeekdaysViewHolder(itemView: View) : TicketsViewHolder(itemView) {

        private val weekdays: TextView = itemView.findViewById(R.id.tickets_weekdays)
        private val disclaimer: TextView = itemView.findViewById(R.id.ticket_weekdays_disclaimer)

        fun bind(w: Weekdays) {
            weekdays.text = formatWeekdays(w)
            disclaimer.text = w.disclaimer
            disclaimer.isGone = w.disclaimer.isEmpty()
        }

        private fun formatWeekdays(item: Weekdays): String {
            val sb = StringBuilder()
            item.weekdays.forEachIndexed { index, weekday ->
                if (index == 0) {
                    sb.append(weekday.name)
                } else if (index < item.weekdays.size - 1) {
                    sb.append(", ${weekday.name.toLowerCase()}")
                } else {
                    sb.append(" e ${weekday.name.toLowerCase()}")
                }
            }
            return sb.toString()
        }

    }

    class TickerViewHolder(itemView: View) : TicketsViewHolder(itemView) {

        private val label: TextView = itemView.findViewById(R.id.tickets_format_label)
        private val value: TextView = itemView.findViewById(R.id.tickets_format_value)

        fun bind(ticket: Ticket) {
            label.text = formatFormat(ticket.format)
            value.text =
                itemView.resources.getString(
                    R.string.tickets_value,
                    formatPrice(ticket.full),
                    formatPrice(ticket.half)
                )
        }

        private fun formatFormat(format: String): String {
            // TODO: Move strings to strings.xml
            return when (format) {
                VideoFormat2D -> "2D"
                VideoFormat3D -> "3D"
                else -> "2D ou 3D"
            }
        }

        private fun formatPrice(price: Float): String {
            return String.format(BRAZIL, "%.2f", price)
        }

    }

}
