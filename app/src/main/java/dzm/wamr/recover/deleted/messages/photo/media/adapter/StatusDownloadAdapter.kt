package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.DownloadItemBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.getBack
import java.io.File

class StatusDownloadAdapter(
    var mData: ArrayList<StatusDataModel>,
    val listener:AdapterClicklistener
) : RecyclerView.Adapter<StatusDownloadAdapter.ViewHolder>() {
    private var file: File? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding:DownloadItemBinding=DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.download_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val jpast = mData[position]
        file = File(jpast.filePath)
        if (!file!!.isDirectory) {
            if (!getBack(
                    jpast.filePath,
                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                ).isEmpty()
            ) {
                try {
                    Glide.with(holder.binding.root.context).load(file).apply(
                        RequestOptions().placeholder(R.color.black).error(android.R.color.black)
                            .optionalTransform(
                                RoundedCorners(1)
                            )
                    ).into(holder.binding.imageView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                holder.binding.iconplayer.visibility = View.VISIBLE
            } else if (!getBack(
                    jpast.filePath,
                    "((\\.3ga|\\.aac|\\.aif|\\.aifc|\\.aiff|\\.amr|\\.au|\\.aup|\\.caf|\\.flac|\\.gsm|\\.kar|\\.m4a|\\.m4p|\\.m4r|\\.mid|\\.midi|\\.mmf|\\.mp2|\\.mp3|\\.mpga|\\.ogg|\\.oma|\\.opus|\\.qcp|\\.ra|\\.ram|\\.wav|\\.wma|\\.xspf)$)"
                ).isEmpty()
            ) {
                holder.binding.iconplayer.visibility = View.GONE
            } else if (!getBack(
                    jpast.filePath,
                    "((\\.jpg|\\.png|\\.gif|\\.jpeg|\\.bmp)$)"
                ).isEmpty()
            ) {
                holder.binding.iconplayer.visibility = View.GONE
                Glide.with(holder.binding.root.context).load(file).apply(
                    RequestOptions().placeholder(R.color.black).error(android.R.color.black)
                        .optionalTransform(
                            RoundedCorners(1)
                        )
                ).into(holder.binding.imageView)
            }

        }


        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(val binding:DownloadItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.cardView.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.shareIV.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.deleteIV.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }


}