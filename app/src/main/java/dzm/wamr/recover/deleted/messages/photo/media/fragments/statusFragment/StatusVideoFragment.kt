package dzm.wamr.recover.deleted.messages.photo.media.fragments.statusFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.VideoStatusAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentStatusVideoBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.Common
import dzm.wamr.recover.deleted.messages.photo.media.util.LoadingDataClass
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@DelicateCoroutinesApi
class StatusVideoFragment : Fragment() {


    private lateinit var binding: FragmentStatusVideoBinding
    var videoStatusAdapter: VideoStatusAdapter? =null
    private val MP4 = ".mp4"
    private val videoList: ArrayList<Status> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatusVideoBinding.inflate(layoutInflater)
        val view: View = binding.root
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

//        binding.swipeRefreshLayout.setColorSchemeColors(
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
//            ContextCompat.getColor(requireActivity(), R.color.design_default_color_on_primary),
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
//        )

//        binding.swipeRefreshLayout.setOnRefreshListener { getStatus() }

        binding.recyclerViewVideo.setHasFixedSize(true)
        binding.recyclerViewVideo.layoutManager = GridLayoutManager(activity, Common.GRID_COUNT)
        videoStatusAdapter = VideoStatusAdapter(videoList,object :AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                if (view.id == R.id.ivThumbnail)
                {
                    performStatusThumnailClick(videoList.get(position))
                }else if (view.id == R.id.save)
                {
                    performStatusSave(videoList.get(position))
                }
            }
        })
        binding.recyclerViewVideo.adapter = videoStatusAdapter

        getStatus()
        return view
    }

    private fun performStatusThumnailClick(status: Status) {
        val inflater = LayoutInflater.from(binding.root.context)
        val view1: View = inflater.inflate(R.layout.view_video_full_screen, null)
        val alertDg = AlertDialog.Builder(binding.root.context)
//            val mediaControls =
//                view1.findViewById<FrameLayout>(R.id.videoViewWrapper)
        if (view1.parent != null) {
            (view1.parent as ViewGroup).removeView(view1)
        }
        alertDg.setView(view1)
        val videoView =
            view1.findViewById<VideoView>(R.id.videoView)
        val mediaController =
            MediaController(binding.root.context, false)
        videoView.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
            mediaController.show(0)
            mp.isLooping = true
        }
        videoView.setMediaController(mediaController)
        mediaController.setMediaPlayer(videoView)
        videoView.setVideoURI(status.fileUri)
        videoView.requestFocus()
        (mediaController.parent as ViewGroup).removeView(mediaController)
//            if (mediaControls.parent != null) {
//                mediaControls.removeView(mediaController)
//            }
//            mediaControls.addView(mediaController)
        val alert2 = alertDg.create()
        alert2.window!!.attributes.windowAnimations = R.style.SlidingDialogAnimation
        alert2.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alert2.window!!
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert2.show()
        MyAppOpenAd.isDialogClose=false
        alert2.setOnDismissListener {
            MyAppOpenAd.isDialogClose=true
        }
    }

    private fun performStatusSave(status: Status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Glide.with(binding.root.context)
                .asBitmap()
                .load(status.fileUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        val APP_DIR11: String? =
                            binding.root.context.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)?.toString()
                        Common.copyFileFromUri(
                            status,
                            binding.root.context,
                            resource,
                            status.title!!,
                            APP_DIR11!!
                        )
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

        }
        else
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            val file = File(
                Environment.getExternalStorageDirectory(),
                "RecoveryApp${File.separator}WhatsRecovery Statuses"
            )
            val fileName: String
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())
            fileName = if (status.isVideo) {
                "VID_$currentDateTime.mp4"
            } else {
                "IMG_$currentDateTime.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            status.file?.let { it1 -> Common.copySingleFile(it1, destFile) }
//                Common.copyFile(status, container.context, container)
            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.this_is_saved))
            Log.d("onResourceReady", "onResourceReady: running")
        } else {
            Common.copyFile( status, binding.root.context)
        }
    }

    fun getStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getVideosApi29()
        } else if (Common.STATUS_DIRECTORY.exists()) {
            execute()
        } else {
            binding.messageTextVideo.visibility = View.VISIBLE
            binding.messageTextVideo.setText(binding.root.context.getString(R.string.no_deleted_media))
            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.no_deleted_media))
//            binding.swipeRefreshLayout.setRefreshing(false)
        }
    }


    private fun getVideosApi29() {
        videoList.clear()
         videoList.addAll(LoadingDataClass.getVideoData())
        if (videoList.size <=0) {
            binding.messageTextVideo.visibility = View.VISIBLE
            binding.messageTextVideo.setText(binding.root.context.getString(R.string.no_deleted_media))
            binding.prgressBarVideo.visibility = View.GONE
            return
        }

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                if (videoList.size <= 0) {
                    binding.messageTextVideo.visibility = View.VISIBLE
                    binding.messageTextVideo.setText(binding.root.context.getString(R.string.no_status_file_found))
                    binding.prgressBarVideo.visibility = View.GONE
                } else {
                    binding.messageTextVideo.visibility = View.GONE
                    binding.messageTextVideo.text = ""
                }

                videoStatusAdapter?.notifyDataSetChanged()
                binding.prgressBarVideo.visibility = View.GONE
            }
        }
//        binding.swipeRefreshLayout.isRefreshing = false
    }


    @SuppressLint("NotifyDataSetChanged")
    @OptIn(DelicateCoroutinesApi::class)
    private fun execute() {
        Thread {
            val statusFiles: Array<File> = Common.STATUS_DIRECTORY.listFiles()
            videoList.clear()
            if (statusFiles.isNotEmpty()) {
                Arrays.sort(statusFiles)
                for (file in statusFiles) {
                    val status = Status(file, file.name, file.absolutePath, file.name.endsWith(MP4), null)
                    if (status.isVideo && status.title!!.endsWith(".mp4")) {
                        videoList.add(status)
                    }
                }
                //handler.post(Runnable {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        if (videoList.size <= 0) {
                            binding.messageTextVideo.visibility = View.VISIBLE
                            binding.messageTextVideo.setText(binding.root.context.getString(R.string.no_status_file_found))
                        } else {
                            binding.messageTextVideo.visibility = View.GONE
                            binding.messageTextVideo.text = ""
                        }

                        videoStatusAdapter?.notifyDataSetChanged()
                        binding.prgressBarVideo.visibility = View.GONE
                    }
                }
                //})
            } else {
                //handler.post(Runnable {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        binding.prgressBarVideo.visibility = View.GONE
                        binding.messageTextVideo.visibility = View.VISIBLE
                        binding.messageTextVideo.setText(binding.root.context.getString(R.string.no_status_file_found))
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.no_status_file_found))
                    }
                }
                //})
            }
//            binding.swipeRefreshLayout.isRefreshing = false
        }.start()
    }

}