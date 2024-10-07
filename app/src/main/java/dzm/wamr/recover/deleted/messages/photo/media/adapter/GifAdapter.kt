package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.RowItemEmojiBinding
import dzm.wamr.recover.deleted.messages.photo.media.databinding.RowItemGifBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import java.util.*

class GifAdapter(private val list: ArrayList<String>, val listener:AdapterClicklistener) :
    RecyclerView.Adapter<GifAdapter.ViewHolder>() {
    class ViewHolder(val binding:RowItemGifBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.ivShare.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:RowItemGifBinding=DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_item_gif,parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: String = list.get(position)

        holder.binding.ivImage.controller= Utils.frescoGifLoad(Utils.convertIntoGif(model),
            R.drawable.ractengle_round_solid_light_gray,
            holder.binding.ivImage)

        holder.bind(listener,position)
    }

}