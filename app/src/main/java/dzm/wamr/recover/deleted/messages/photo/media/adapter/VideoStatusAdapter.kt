package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ItemStatusBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.Common
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoStatusAdapter(private val videoList: ArrayList<Status>, val listener:AdapterClicklistener) :
    RecyclerView.Adapter<VideoStatusAdapter.ViewHolder>() {

    class ViewHolder(val binding:ItemStatusBinding) : RecyclerView.ViewHolder( binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.ivThumbnail.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.save.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding:ItemStatusBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_status, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status: Status = videoList.get(position)
        holder.binding.playImg.visibility = View.VISIBLE


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Glide.with(holder.binding.root.context)
                .asBitmap()
                .load(status.fileUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        holder.binding.ivThumbnail.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

        }
        else
        {
            Glide.with(holder.binding.root.context).load(status.file).into(holder.binding.ivThumbnail)
        }


        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

}