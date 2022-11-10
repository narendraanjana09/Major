package com.nsa.major.home.adapters


import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nsa.major.R
import com.nsa.major.databinding.ChatDateItemBinding
import com.nsa.major.databinding.ChatItemReceivedBinding
import com.nsa.major.databinding.ChatItemSentBinding
import com.nsa.major.home.models.MessageModel
import com.nsa.major.util.Constants
import com.nsa.major.util.Util


class MessagesListAdapter internal constructor(
    private val teacherId: String?,
    private val messagesList: ArrayList<MessageModel>,
    private val userId: String
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val holderTypeMessageIsDate = 0
    private val holderTypeMessageReceived = 1
    private val holderTypeMessageSent = 2

    inner class ReceivedViewHolder(private val binding: ChatItemReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageModel) {
            binding.timeText.text=Util.getTime(item.time!!)
            binding.userName.text=item.name
            if(item.senderId==teacherId){
                val img: Drawable =
                    binding.userName.resources.getDrawable(R.drawable.teacher_svg)
                binding.userName.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
            }
            binding.messageText.text = item.message
        }
    }
    class DateViewHolder(private val binding: ChatDateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageModel) {
            binding.dateTv.text=item.message
        }
    }

    class SentViewHolder(private val binding: ChatItemSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageModel) {
            binding.timeText.text=Util.getTime(item.time!!)
            binding.messageText.text = item.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(messagesList[position].senderId==Constants.IS_DATE){
            Log.e("TAG", "getItemViewType: is Date ${messagesList[position].message} ")
            holderTypeMessageIsDate
        }else if (messagesList[position].senderId != userId) {
            holderTypeMessageReceived
        } else  {
            holderTypeMessageSent
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            holderTypeMessageSent -> (holder as SentViewHolder).bind(
                messagesList[position]
            )
            holderTypeMessageReceived -> (holder as ReceivedViewHolder).bind(
                messagesList[position]
            )
            holderTypeMessageIsDate-> (holder as DateViewHolder).bind(
                messagesList[position]
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            holderTypeMessageSent -> {
                val binding = ChatItemSentBinding.inflate(layoutInflater, parent, false)
                SentViewHolder(binding)
            }
            holderTypeMessageReceived -> {
                val binding = ChatItemReceivedBinding.inflate(layoutInflater, parent, false)
                ReceivedViewHolder(binding)
            }
            holderTypeMessageIsDate->{
                val binding=ChatDateItemBinding.inflate(layoutInflater,parent,false)
                DateViewHolder(binding)
            }
            else -> {
                throw Exception("Error reading holder type")
            }
        }
    }

    override fun getItemCount(): Int {
       return messagesList.size
    }
}