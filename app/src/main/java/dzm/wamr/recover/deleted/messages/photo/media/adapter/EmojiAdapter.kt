package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.RowItemEmojiBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import java.util.*

class EmojiAdapter(private val list: ArrayList<String>, val listener:AdapterClicklistener) :
    RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {
    class ViewHolder(val binding:RowItemEmojiBinding) :
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
        val binding:RowItemEmojiBinding=DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_item_emoji,parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: String = list.get(position)

        try {
            holder.binding.title.setText(model)
        } catch (e: Exception) {
            holder.binding.title.setText("")
        }

        holder.bind(listener,position)
    }

}