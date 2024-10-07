package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.VideoPlayerActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.DownloadAdapter.*
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.DownloadItemsBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.LoadingDataClass
import kotlin.collections.ArrayList

class DownloadAdapter(
    private val saveMediaList: ArrayList<Status>, val listener:AdapterClicklistener
) : RecyclerView.Adapter<ViewHolder>() {

    class ViewHolder(val binding:DownloadItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.download.setOnClickListener {
                listener.onItemClick(it,position)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:DownloadItemsBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.download_items, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaImg: Status = saveMediaList[position]
//        holder.bindFileItems(context, saveMediaList[position])
        Glide.with(holder.itemView.context).load(mediaImg.fileUri).placeholder(R.drawable.media_sreen_pic)
            .into(holder.binding.download)
        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return saveMediaList.size
    }

}