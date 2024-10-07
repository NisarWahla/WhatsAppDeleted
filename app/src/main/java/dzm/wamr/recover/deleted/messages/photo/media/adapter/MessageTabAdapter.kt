package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ReminderViewLayoutBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.DataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageTabAdapter(val list:ArrayList<DataModel>, val listener:AdapterClicklistener) : RecyclerView.Adapter<MessageTabAdapter.MyViewHolder>() {


    private val currentDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding:ReminderViewLayoutBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.reminder_view_layout,parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: DataModel = list.get(position)
        holder.binding.nameOUserTv.text = model.name
        holder.binding.phoneOUserTv.text = model.name
        if (Utils.isNumeric("${model.name}"))
        {
            holder.binding.phoneOUserTv.visibility=View.VISIBLE
            holder.binding.nameOUserTv.visibility=View.GONE
        }
        else
        {
            holder.binding.phoneOUserTv.visibility=View.GONE
            holder.binding.nameOUserTv.visibility=View.VISIBLE
        }

        holder.binding.msgOUserTv.text = model.message
        holder.binding.dateTv.text = if (Objects.equals(model.date, currentDate))
            model.time
        else
            model.date


        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun filter(updatedList: ArrayList<DataModel>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }
    inner class MyViewHolder(val binding:ReminderViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.rl.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))

            binding.deleteChat.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }
}