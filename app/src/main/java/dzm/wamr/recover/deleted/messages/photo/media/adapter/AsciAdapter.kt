package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.RowItemAsciBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import java.util.*

class AsciAdapter(private val list: ArrayList<String>,val listener:AdapterClicklistener) :
    RecyclerView.Adapter<AsciAdapter.ViewHolder>() {
    class ViewHolder(val binding: RowItemAsciBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.ivShare.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.ivCopy.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:RowItemAsciBinding=DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_item_asci,parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: String = list[position]
        holder.binding.title.text = model
        holder.bind(listener,position)
    }

}