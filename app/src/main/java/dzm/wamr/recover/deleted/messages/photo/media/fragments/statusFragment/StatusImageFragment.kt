package dzm.wamr.recover.deleted.messages.photo.media.fragments.statusFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.VideoPlayerActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ImageStatusAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentStatusImageBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.StatusFragment
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.Common
import dzm.wamr.recover.deleted.messages.photo.media.util.LoadingDataClass
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@DelicateCoroutinesApi
class StatusImageFragment : Fragment() {


    private lateinit var binding: FragmentStatusImageBinding
    private lateinit var imageStatusAdapter: ImageStatusAdapter
    private val imagesList: ArrayList<Status> = ArrayList()
    private val MP4 = ".mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insatnce = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStatusImageBinding.inflate(layoutInflater)
        val view: View = binding.root
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

//        binding.swipeRefreshLayout.setColorSchemeColors(
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
//            ContextCompat.getColor(requireActivity(), R.color.teal_200),
//            ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
//        )

//        binding.swipeRefreshLayout.setOnRefreshListener { getStatus() }

        binding.recyclerViewImage.setHasFixedSize(true)
        binding.recyclerViewImage.layoutManager = GridLayoutManager(activity, Common.GRID_COUNT)
        Log.i("TAG", "onCreateView: " + "getStatus")
        getStatus()
        return view
    }

    fun getStatus() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                getImagesApi29()
            }
            Common.STATUS_DIRECTORY.exists() -> {
                execute()
            }
            else -> {
                binding.messageTextView.visibility = View.VISIBLE
                binding.messageTextView.setText(binding.root.context.getString(R.string.no_deleted_media))
                Utils.showToast(binding.root.context, binding.root.context.getString(R.string.no_deleted_media))
//                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBarImage.visibility = View.GONE
            }
        }
    }

    var mContext: Context? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getImagesApi29() {
        imagesList.clear()
        imagesList.addAll(LoadingDataClass.getStatusImages())
        if (imagesList.size <= 0) {
            binding.messageTextView.visibility = View.VISIBLE
            binding.messageTextView.setText(binding.root.context.getString(R.string.no_deleted_media))
            binding.progressBarImage.visibility = View.GONE
            return
        }

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                if (imagesList.size <= 0) {
                    binding.messageTextView.visibility = View.VISIBLE
                    binding.messageTextView.setText(binding.root.context.getString(R.string.no_status_file_found))
                    binding.progressBarImage.visibility = View.GONE
                } else {
                    binding.messageTextView.visibility = View.GONE
                    binding.messageTextView.text = ""
                    binding.progressBarImage.visibility = View.GONE
                }
                imageStatusAdapter = ImageStatusAdapter(
                    imagesList,
                    object : AdapterClicklistener {
                        override fun onItemClick(view: View, position: Int) {
                            if (view.id==R.id.download)
                            {
                                shareContentViaIntent(imagesList.get(position))
                            }
                        }
                    }
                )
                binding.recyclerViewImage.adapter = imageStatusAdapter
                imageStatusAdapter.notifyDataSetChanged()
                binding.progressBarImage.visibility = View.GONE
            }
        }
//            binding.swipeRefreshLayout.isRefreshing = false

    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun execute() {
        val statusFiles: Array<File?>
        statusFiles = Common.STATUS_DIRECTORY.listFiles()
        imagesList.clear()
        if (statusFiles != null && statusFiles.size > 0) {
            Arrays.sort(statusFiles)
            for (file in statusFiles) {
                val status =
                    Status(file, file!!.name, file.absolutePath, file.name.endsWith(MP4), null)
                if (!status.isVideo && status.title!!.endsWith(".jpg")) {
                    imagesList.add(status)
                }
            }
            //handler?.post {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (imagesList.size <= 0) {
                        binding.messageTextView.visibility = View.VISIBLE
                        binding.messageTextView.setText(binding.root.context.getString(R.string.no_status_file_found))
                    } else {
                        binding.messageTextView.visibility = View.GONE
                        binding.messageTextView.text = ""
                    }
                    imageStatusAdapter = ImageStatusAdapter(
                        imagesList,
                        object : AdapterClicklistener {
                            override fun onItemClick(view: View, position: Int) {
                                if (view.id==R.id.download)
                                {
                                    shareContentViaIntent(imagesList.get(position))
                                }
                            }
                        }
                    )
                    binding.recyclerViewImage.adapter = imageStatusAdapter
                    imageStatusAdapter.notifyDataSetChanged()
                    binding.progressBarImage.visibility = View.GONE
                }
            }
            //}
        } else {
            //handler?.post {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    binding.progressBarImage.visibility = View.GONE
                    binding.messageTextView.visibility = View.VISIBLE
                    binding.messageTextView.setText(binding.root.context.getString(R.string.no_status_file_found))
                    Utils.showToast(binding.root.context, binding.root.context.getString(R.string.no_status_file_found))
                }
            }
            //}
        }
//        binding.swipeRefreshLayout.isRefreshing = false
    }

    val savedStatus: ArrayList<Status> = ArrayList()
    private fun shareContentViaIntent(mediaImg: Status) {
        if (AdConst.IS_AD_SHOW) {
            if (mediaImg.isVideo) {
                savedStatus.add(mediaImg)
                val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                intent.putParcelableArrayListExtra("path", savedStatus)
                binding.root.context.startActivity(intent)
            } else {
                imageDialog(binding.root.context, mediaImg, false)
            }
        } else {
            if (mediaImg.isVideo) {
                savedStatus.add(mediaImg)
                val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                intent.putParcelableArrayListExtra("path", savedStatus)
                binding.root.context.startActivity(intent)
            } else {
                imageDialog(binding.root.context, mediaImg, false)
            }
        }
    }

    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }

    fun imageDialog(context: Context, mediaImg: Status, isStatus: Boolean) {
        val APP_DIR11: String? =
            context.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)
                ?.toString()
        val alertD = AlertDialog.Builder(context, R.style.myFullscreenAlertDialogStyle)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.view_image_full_screen, null)
        alertD.setView(view)
        val share = view.findViewById<LinearLayout>(R.id.shareImg)
        val setWall = view.findViewById<LinearLayout>(R.id.setWall)
        val saveImg = view.findViewById<LinearLayout>(R.id.saveImg)

        //val adViewRl = view.findViewById<RelativeLayout>(R.id.adViewRImg)
        val imageView = view.findViewById<ImageView>(R.id.img)
        if (!isStatus) {
            saveImg.visibility = View.GONE
        }
        /* AdManger.loadBannerAds(adViewRl, activity)
         if (isConnected(context)) {
             adViewRl.visibility = View.VISIBLE
         } else {
             adViewRl.visibility = View.GONE
         }*/
        setWall.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.set_wallpaper_dialog, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.setOnShowListener {
                (view.parent as ViewGroup).background = ColorDrawable(Color.TRANSPARENT)
            }
            bottomSheetDialog.show()

            val setHome = view.findViewById<TextView>(R.id.tv_set_home_screen)
            val setLock = view.findViewById<TextView>(R.id.tv_set_lock_screen)
            val setBoth = view.findViewById<TextView>(R.id.tv_set_both)

            setHome.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setWallpaper(context, mediaImg.fileUri!!, 0, view)
                } else {
                    setWallpaper(context, mediaImg.path!!, 0, view)
                }
                bottomSheetDialog.dismiss()
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                setLock.visibility = View.GONE
            } else {
                setLock.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setWallpaper(context, mediaImg.fileUri!!, 1, view)
                    } else {
                        setWallpaper(context, mediaImg.path!!, 1, view)
                    }

                    bottomSheetDialog.dismiss()
                }
            }
            setBoth.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setWallpaper(context, mediaImg.fileUri!!, 2, view)
                } else {
                    setWallpaper(context, mediaImg.path!!, 2, view)
                }

                bottomSheetDialog.dismiss()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Glide.with(context).asBitmap().load(mediaImg.fileUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        imageView.setImageBitmap(resource)


                        share.setOnClickListener {
                            val APP_DIR11: String? =
                                context.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)
                                    ?.toString()
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "image/jpg"
                            val uri: File = Common.getFileFromUri(
                                mediaImg, context,

                                resource, mediaImg.title!!, APP_DIR11!!
                            )
                            val ur: Uri = FileProvider.getUriForFile(
                                context, context.packageName.toString() + ".provider", uri
                            )
                            shareIntent.putExtra(Intent.EXTRA_STREAM, ur)
                            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            MyAppOpenAd.isDialogClose=false
                            resultShareCallback.launch(
                                Intent.createChooser(
                                shareIntent, binding.root.context.getString(R.string.share_image)
                            ))
                        }
                        saveImg.setOnClickListener {


//                                Log.d("onResourceReady", "onResourceReady: running")
                            if (Common.checkImageValue(mediaImg, APP_DIR11!!)) {
                                Utils.showToast(binding.root.context, binding.root.context.getString(R.string.file_already_exists))
                            } else {


                                Common.copyFileFromUri(
                                    mediaImg, context, resource, mediaImg.title!!, APP_DIR11!!
                                )
                                StatusFragment.RefreshDownloads()
                            }

                        }

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })


        } else {
            Glide.with(context).load(mediaImg.path).into(imageView)
            share.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/jpg"
//                shareIntent.putExtra(
//                    Intent.EXTRA_STREAM,
//                    mediaImg.fileUri
//                )
                val ur: Uri = FileProvider.getUriForFile(
                    context, context.packageName.toString() + ".provider", mediaImg.file!!
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, ur)
                shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                MyAppOpenAd.isDialogClose=false
                resultShareCallback.launch( Intent.createChooser(
                    shareIntent, binding.root.context.getString(R.string.share_image)
                ))

            }

        }

        saveImg.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "RecoveryApp${File.separator}WhatsRecovery Statuses"
                )
                if (!file.exists()) {
                    file.mkdirs()
                }
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateTime = sdf.format(Date())
                val fileName: String = if (mediaImg.isVideo) {
                    "${mediaImg.title}.mp4"
                } else {
                    "${mediaImg.title}.jpg"
                }
                val destFile = File(file.toString())
                mediaImg.file?.let { it1 -> Common.copySingleFile(it1, file) }
//                Common.copyFile(status, container.context, container)
                StatusFragment.RefreshDownloads()
                Utils.showToast(binding.root.context, binding.root.context.getString(R.string.status_saved_successfully))
            } else {
                Common.copyFile(mediaImg, context)
            }

        }

        val alert = alertD.create()
        alert.window!!.attributes.windowAnimations = R.style.SlidingDialogAnimation
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE)

//            alert.window!!
//                .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()
        MyAppOpenAd.isDialogClose=false
        alert.setOnDismissListener {
            MyAppOpenAd.isDialogClose=true
        }
    }




    private fun setWallpaper(context: Context, body: Uri, check: Int, view: View): Boolean {
//        initProgressDialog(view)
        Glide.with(context).asBitmap().load(body).into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(
                bitmap: Bitmap, transition: Transition<in Bitmap>?
            ) {
                val manager = WallpaperManager.getInstance(context)
                if (check == 0) {
                    try {
                        manager.setBitmap(bitmap)
//                            progressDialog!!.dismiss()
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_home))
                    } catch (ex: Exception) {
                        ex.printStackTrace()
//                            progressDialog!!.dismiss()
                    }
                } else if (check == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
//                                progressDialog!!.dismiss()
                            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_lock))
                        } catch (e: Exception) {
                            e.printStackTrace()
//                                progressDialog!!.dismiss()
                        }
                    }
                } else if (check == 2) {
                    try {
                        manager.setBitmap(bitmap)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
//                            progressDialog!!.dismiss()
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_both))
                    } catch (e: Exception) {
                        e.printStackTrace()
//                            progressDialog!!.dismiss()
                    }
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // this is called when imageView is cleared on lifecycle call or for
                // some other reason.
                // if you are referencing the bitmap somewhere else too other than this imageView
                // clear it here as you can no longer have the bitmap
            }
        })

        return true
    }


    private fun setWallpaper(context: Context, body: String, check: Int, view: View): Boolean {
//        initProgressDialog(view)
        Glide.with(context).asBitmap().load(body).into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(
                bitmap: Bitmap, transition: Transition<in Bitmap>?
            ) {
                val manager = WallpaperManager.getInstance(context)
                if (check == 0) {
                    try {
                        manager.setBitmap(bitmap)
//                            progressDialog!!.dismiss()
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_home))
                    } catch (ex: Exception) {
                        ex.printStackTrace()
//                            progressDialog!!.dismiss()
                    }
                } else if (check == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
//                                progressDialog!!.dismiss()
                            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_lock))
                        } catch (e: Exception) {
                            e.printStackTrace()
//                                progressDialog!!.dismiss()
                        }
                    }
                } else if (check == 2) {
                    try {
                        manager.setBitmap(bitmap)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
//                            progressDialog!!.dismiss()
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.set_both))
                    } catch (e: Exception) {
                        e.printStackTrace()
//                            progressDialog!!.dismiss()
                    }
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // this is called when imageView is cleared on lifecycle call or for
                // some other reason.
                // if you are referencing the bitmap somewhere else too other than this imageView
                // clear it here as you can no longer have the bitmap
            }
        })

        return true
    }


    companion object {

        var insatnce: StatusImageFragment? = null

    }
}