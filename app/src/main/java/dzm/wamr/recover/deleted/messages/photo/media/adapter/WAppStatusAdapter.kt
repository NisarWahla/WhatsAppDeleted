package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.StatusItemBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.getBack

class WAppStatusAdapter(val mData: ArrayList<StatusDataModel>,val isComingFromGalleryOrStatus:Boolean, val listener: AdapterClicklistener)
    : RecyclerView.Adapter<WAppStatusAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding:StatusItemBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.status_item, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jpast = mData.get(position)

        if (isComingFromGalleryOrStatus) {
            holder.binding.downloadIV.visibility = View.GONE
            holder.binding.shareIcon.visibility = View.GONE
        } else {
            holder.binding.downloadIV.visibility = View.GONE
            holder.binding.shareIcon.visibility = View.VISIBLE
        }
        if (!(getBack(jpast.filePath,
                "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)")
                .isEmpty())
        ) {
            holder.binding.iconplayer.visibility = View.VISIBLE
        } else {
            holder.binding.iconplayer.visibility = View.GONE
        }

        if (!(getBack(jpast.filePath, "((\\.mp3)$)").isEmpty())) {
            Glide.with(holder.binding.root.context).load(R.drawable.ic_music).apply(
                RequestOptions().placeholder(R.color.black).error(android.R.color.black)
                    .optionalTransform(
                        RoundedCorners(5)
                    )
            ).into(holder.binding.imageView)
        }
        else
        {
            Glide.with(holder.binding.root.context).load(jpast.filePath).apply(
                RequestOptions().placeholder(R.color.black).error(android.R.color.black)
                    .optionalTransform(
                        RoundedCorners(5)
                    )
            ).into(holder.binding.imageView)
        }


        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(val binding:StatusItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(listener:AdapterClicklistener,position:Int)
        {
            binding.shareIcon.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.downloadIV.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.cardView.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }
}