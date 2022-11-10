package com.nsa.major.home.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nsa.major.databinding.ClassesLayoutItemBinding
import com.nsa.major.home.models.ClassModel

class SingleTextAdapter(
    private val mList: List<String>,
    private val isAttendance: Boolean,
    private val clickCallback: ClickCallback
    ) : RecyclerView.Adapter<SingleTextAdapter.ViewHolder>() {

    interface ClickCallback{
        fun onClicked(position: Int)
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val binding=ClassesLayoutItemBinding.inflate(LayoutInflater.from(parent.context))

        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = mList[position]
        holder.setData(item)


    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    inner class ViewHolder(val binding: ClassesLayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: String) {
            if(isAttendance){
                binding.textView.text=Html.fromHtml(item)
            }else{
                binding.textView.text=item
            }
            binding.root.setOnClickListener {
                clickCallback.onClicked(adapterPosition)
            }
        }


    }
}