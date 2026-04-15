package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Message
import com.example.myapplication.network.User
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: List<Message>,
    private val currentUserId: String,
    private val isAdmin: Boolean,
    private val onLongClick: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class ListItem {
        data class DateHeader(val date: String) : ListItem()
        data class MessageItem(val message: Message) : ListItem()
    }

    private var items = mutableListOf<ListItem>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_DATE_HEADER = 3
    }

    init {
        processMessages(messages)
    }

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        processMessages(newMessages)
        notifyDataSetChanged()
    }

    private fun processMessages(newMessages: List<Message>) {
        items.clear()
        if (newMessages.isEmpty()) return

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val headerFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        var lastDate = ""

        for (message in newMessages) {
            val date = try {
                val parsedDate = sdf.parse(message.timestamp ?: "")
                headerFormat.format(parsedDate ?: Date())
            } catch (e: Exception) {
                ""
            }

            if (date != lastDate) {
                items.add(ListItem.DateHeader(getDisplayDate(message.timestamp)))
                lastDate = date
            }
            items.add(ListItem.MessageItem(message))
        }
    }

    private fun getDisplayDate(timestamp: String?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp ?: "") ?: return ""
            
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            val todayYear = calendar.get(Calendar.YEAR)
            
            calendar.time = date
            val msgDay = calendar.get(Calendar.DAY_OF_YEAR)
            val msgYear = calendar.get(Calendar.YEAR)
            
            if (todayYear == msgYear) {
                if (today == msgDay) return "Today"
                if (today - 1 == msgDay) return "Yesterday"
            }
            
            val outFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            outFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ListItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is ListItem.MessageItem -> {
                val message = item.message
                val senderId = when (val sender = message.sender) {
                    is String -> sender
                    is Map<*, *> -> (sender["_id"] ?: sender["id"]) as? String
                    else -> null
                }
                if (senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
                SentViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
                ReceivedViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SentViewHolder -> holder.bind((item as ListItem.MessageItem).message)
            is ReceivedViewHolder -> holder.bind((item as ListItem.MessageItem).message)
            is DateHeaderViewHolder -> holder.bind((item as ListItem.DateHeader).date)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader: TextView = view.findViewById(R.id.tvDateHeader)
        fun bind(date: String) {
            tvDateHeader.text = date
        }
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvEdited: TextView = view.findViewById(R.id.tvEdited)

        fun bind(message: Message) {
            if (message.isDeleted) {
                tvMessage.text = "This message was deleted"
                tvMessage.alpha = 0.5f
                tvMessage.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            } else {
                tvMessage.text = message.content
                tvMessage.alpha = 1.0f
                if (message.isEmergency) {
                    tvMessage.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvMessage.typeface = android.graphics.Typeface.DEFAULT_BOLD
                } else {
                    tvMessage.setTextColor(itemView.context.getColor(android.R.color.black))
                    tvMessage.typeface = android.graphics.Typeface.DEFAULT
                }
            }
            tvTime.text = formatTime(message.timestamp)
            tvEdited.visibility = if (message.isEdited && !message.isDeleted) View.VISIBLE else View.GONE
            
            itemView.setOnLongClickListener {
                onLongClick(message)
                true
            }
        }
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSender: TextView = view.findViewById(R.id.tvSender)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvEdited: TextView = view.findViewById(R.id.tvEdited)

        fun bind(message: Message) {
            val senderName = when (val sender = message.sender) {
                is Map<*, *> -> sender["fullName"] as? String
                else -> "Unknown"
            }
            tvSender.text = senderName

            if (message.isDeleted) {
                tvMessage.text = "This message was deleted"
                tvMessage.alpha = 0.5f
                tvMessage.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            } else {
                tvMessage.text = message.content
                tvMessage.alpha = 1.0f
                if (message.isEmergency) {
                    tvMessage.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvMessage.typeface = android.graphics.Typeface.DEFAULT_BOLD
                } else {
                    tvMessage.setTextColor(itemView.context.getColor(android.R.color.black))
                    tvMessage.typeface = android.graphics.Typeface.DEFAULT
                }
            }
            tvTime.text = formatTime(message.timestamp)
            tvEdited.visibility = if (message.isEdited && !message.isDeleted) View.VISIBLE else View.GONE

            itemView.setOnLongClickListener {
                onLongClick(message)
                true
            }
        }
    }

    private fun formatTime(timestamp: String?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp ?: "")
            val outSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            outSdf.format(date ?: Date())
        } catch (e: Exception) {
            ""
        }
    }
}
