package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.VideoPreviewActivity
import dzm.wamr.recover.deleted.messages.photo.media.databinding.PreviewListItemBinding
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.getBack
import kotlinx.coroutines.flow.combine

class FullscreenImageAdapter(var imageList: ArrayList<StatusDataModel>) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding:PreviewListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(container.context),R.layout.preview_list_item, container, false)
        if (!getBack(
                imageList[position].filePath,
                "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
            ).isEmpty()
        ) {
            binding.iconplayer.visibility = View.VISIBLE
        } else {
            binding.iconplayer.visibility = View.GONE
        }
        Glide.with(binding.root.context).load(imageList[position].filePath).into(binding.imageView)
        binding.imageView.setOnClickListener {
            if (!getBack(
                    imageList[position].filePath,
                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                ).isEmpty()
            ) {
                val intent = Intent(binding.root.context, VideoPreviewActivity::class.java)
                intent.putExtra("videoPath", imageList[position].filePath)
                binding.root.context.startActivity(intent)
            }
        }
        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }
}