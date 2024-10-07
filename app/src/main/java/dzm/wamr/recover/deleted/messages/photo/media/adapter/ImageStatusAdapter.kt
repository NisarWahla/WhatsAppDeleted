package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.VideoPlayerActivity
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.StatusImageItemBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import java.io.File
import kotlin.collections.ArrayList

class ImageStatusAdapter(
    private val statusList: ArrayList<Status>, val listener:AdapterClicklistener
) : RecyclerView.Adapter<ImageStatusAdapter.ViewHolder>() {


    class ViewHolder(val binding:StatusImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.saveImg.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:StatusImageItemBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.status_image_item, parent, false)
        PrefManager(binding.root.context).setStatusList(parent.context, Common.STATUS_LIS, statusList)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val status: Status = statusList[position]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Glide.with(holder.itemView.context).asBitmap().load(status.fileUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        holder.binding.saveImg.setImageBitmap(resource)

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            Glide.with(holder.itemView.context).load(status.file).into(holder.binding.saveImg)
        }
        holder.bind(listener,position)

    }

    override fun getItemCount(): Int {
        return statusList.size
    }



}