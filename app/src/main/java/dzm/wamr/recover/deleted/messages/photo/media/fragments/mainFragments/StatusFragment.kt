package dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.bottomsheet.BottomSheetDialog
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.VideoPlayerActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.DownloadAdapter
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ImageStatusAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentStatusBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.viewModel.SharedViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {
    private val saveMediaList: ArrayList<Status> = ArrayList()
    private var saveAdapter: DownloadAdapter? =null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    private val videoList: ArrayList<Status> = ArrayList()
    private val statusList: ArrayList<Status> = ArrayList()
    private lateinit var imageStatusAdapter: ImageStatusAdapter
    private val statusL: ArrayList<Status> = ArrayList()
    private val MP4 = ".mp4"
    private lateinit var binding: FragmentStatusBinding
    lateinit var destFile: String
    var mContext: Context? = null
    var mActivity: Activity? = null
    var REQUEST_ACTION_OPEN_DOCUMENT_TREE = 101
    lateinit var sharedViewModel: SharedViewModel
    var prefManager: PrefManager? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
        this.mActivity = context as Activity

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mContext = context
        instance = this
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(layoutInflater)
        initControl()
        return binding.root
    }


    private val launcherForPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                val uri: Uri = result.data?.data!!
                Log.e("onActivityResult: ", "" + result.data?.data)

                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    prefManager!!.setWATree(uri.toString())

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }



                populateStatus()
            }
        }

    private fun initControl() {
        prefManager = PrefManager(binding.root.context)
        binding.recyclerStatus.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation= LinearLayoutManager.HORIZONTAL
        binding.recyclerStatus.layoutManager =layoutManager

        LocalBroadcastManager.getInstance(mContext!!)
            .registerReceiver(onNotice, IntentFilter("status"))
        setMediaAdapter()
        getDownloadedStatuses()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            lifecycleScope.launchWhenCreated {
                getStatus()
            }

            binding.load.visibility = View.GONE
        } else {
            binding.load.visibility = View.VISIBLE
        }

        sharedViewModel.getDownloadsStatus().observe(viewLifecycleOwner) {
            saveMediaList.clear()
            saveMediaList.addAll(it)
            saveAdapter?.notifyDataSetChanged()
        }
        destFile =
            (requireContext().getExternalFilesDir(File("RecoveryApp${File.separator}.Statuses").path)
                .toString())
        sharedViewModel.getDocumentedFiles().observe(viewLifecycleOwner) {
            imageStatusAdapter.notifyDataSetChanged()
            //statusL.clear()
            //statusL.addAll(it)
//            for (status in statusL) {
//                try {
//                    Glide.with(requireContext()).asBitmap().load(status.fileUri)
//                        .into(object : CustomTarget<Bitmap?>() {
//                            override fun onResourceReady(
//                                resource: Bitmap, transition: Transition<in Bitmap?>?
//                            ) {
//                                Common.copyStatusesFileFromUri(
//                                    status, requireContext(), resource, status.title!!, destFile
//                                )
//                            }
//
//                            override fun onLoadCleared(placeholder: Drawable?) {
//
//                            }
//                        })
//
//                } catch (e: Exception) {
//                }
//            }
            try {
                //getSavedStatuses()
            } catch (e: Exception) {

            }
        }

        binding.load.setOnClickListener {
            if (Utils.appInstalledOrNot(mContext!!, "com.whatsapp")) {
                val sm = mContext!!.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val statusDir: String = getWhatsupFolder()!!
                var intent: Intent? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                    var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
                    var scheme = uri.toString()
                    scheme = scheme.replace("/root/", "/document/")
                    scheme += "%3A$statusDir"
                    uri = Uri.parse(scheme)
                    intent.putExtra("android.provider.extra.INITIAL_URI", uri)
                } else {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.putExtra(
                        "android.provider.extra.INITIAL_URI",
                        Uri.parse("content://com.android.externalstorage.documents/document/primary%3A$statusDir")
                    )
                }
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                //startActivityForResult(intent, REQUEST_ACTION_OPEN_DOCUMENT_TREE)
                launcherForPermission.launch(intent)
                MyAppOpenAd.isDialogClose=false
            } else {
                Utils.showToast(binding.root.context, binding.root.context.getString(R.string.please_install_first))
            }
        }
        //        refresh()
        binding.swipelayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                suspend {
                    refresh()
                }
                if (binding.swipelayout.isRefreshing) {
                    binding.swipelayout.isRefreshing = false
                }
            }, 100)
        }
        binding.load.isVisible = prefManager!!.getWATree().equals("")
    }




    override fun onResume() {
        super.onResume()
        if (!prefManager!!.getWATree().equals("")) {
            populateStatus()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            MyAppOpenAd.isDialogClose=true
        },300)
    }

    private fun getFromSdcard(): Array<DocumentFile>? {
        val treeUri: String = prefManager!!.getWATree()!!
        val fromTreeUri =
            DocumentFile.fromTreeUri(requireContext().applicationContext, Uri.parse(treeUri))
        return if (fromTreeUri != null && fromTreeUri.exists() && fromTreeUri.isDirectory && fromTreeUri.canRead() && fromTreeUri.canWrite()) {
            fromTreeUri.listFiles()
        } else {
            null
        }
    }

    private fun populateStatus() {
//        Observable.fromCallable<Array<DocumentFile>> {
//            return@fromCallable getFromSdcard()!!
//        }.subscribeOn(Schedulers.io()).subscribe { item ->
//            val statusList: ArrayList<Status> = ArrayList()
//            item.forEachIndexed { index, documentFile ->
//                if (!documentFile.uri.toString().contains(".nomedia")) {
//                    Log.e(TAG, "path" + documentFile.uri.toString())
//                    var isVideo = getMimeType(mContext!!, documentFile.uri).equals("mp4")
//                    statusList.add(
//                        Status(
//                            File(documentFile.uri.toString()),
//                            documentFile.name,
//                            documentFile.uri.toString(),
//                            isVideo,
//                            documentFile.uri
//                        )
//                    )
//                }
//            }
//            sharedViewModel.updateAllDocument(statusList)
//        }
        Observable.fromCallable<Array<DocumentFile>> {
            return@fromCallable getFromSdcard()!!
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : io.reactivex.rxjava3.core.Observer<Array<DocumentFile>> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {
                }

                override fun onNext(t: Array<DocumentFile>) {
                    t.forEachIndexed { index, documentFile ->
                        Log.e(TAG, "onNext " + documentFile.uri.toString())
                    }
                    imageStatusAdapter.notifyDataSetChanged()
                }

            })
    }

    fun getMimeType(context: Context, uri: Uri): String? {

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        return extension
    }

    fun getWhatsupFolder(): String? {
        return if (File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
            ).isDirectory
        ) {
            "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        } else {
            "WhatsApp%2FMedia%2F.Statuses"
        }
    }


    private fun setMediaAdapter() {
        //media recycler
        saveAdapter = DownloadAdapter(saveMediaList,object :AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                if (view.id==R.id.download)
                {
                    shareContentViaIntent(saveMediaList.get(position))
                }
            }
        })
        binding.recyclerDownload.layoutManager = GridLayoutManager(mContext, 3)
        binding.recyclerDownload.adapter = saveAdapter
        imageStatusAdapter = ImageStatusAdapter(statusList, object:AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                if (view.id==R.id.save_img)
                {
                    shareItemViaIntent(statusList.get(position))
                }
            }
        })
        binding.recyclerStatus.adapter = imageStatusAdapter
    }

    private fun shareItemViaIntent(status: Status) {
        if (AdConst.IS_AD_SHOW) {
            if (status.isVideo) {
                savedStatus.add(status)
                val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                intent.putParcelableArrayListExtra("path", savedStatus)
                intent.putExtra("from", "status")
                binding.root.context.startActivity(intent)
            } else {
                imageDialog(binding.root.context, status, true)
            }
        } else {
            if (status.isVideo) {
                savedStatus.add(status)
                val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                intent.putParcelableArrayListExtra("path", savedStatus)
                intent.putExtra("from", "status")
                binding.root.context.startActivity(intent)
            } else {
                imageDialog(binding.root.context, status, true)
            }
        }
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
                            resultShareCallback.launch(Intent.createChooser(
                                shareIntent, "Share image"
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
                Log.d("onResourceReady", "onResourceReady: running")
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



    fun getDownloadedStatuses() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            saveMediaList.clear()
            //pass video data to adapter list

            CoroutineScope(Dispatchers.IO).launch {
                val lis = async {
                    File(
                        mContext!!.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)
                            ?.toString()!!
                    ).listFiles()
                }
                val it = lis.await()
                if (it != null) {
                    sorData(it)
                    for (i in it.indices) {
                        val file: File = it[i]
                        val lUri = file.toUri()
                        val ext = file.extension
                        var type: Boolean = ext == "mp4"
                        saveMediaList.add(Status(file, file.name, file.path, type, lUri))
                    }
                    withContext(Dispatchers.Main) {
                        saveMediaList.reverse()
                        saveAdapter?.notifyDataSetChanged()

                    }


                }

            }

        } else {
            saveMediaList.clear()
            //pass video data to adapter list
            CoroutineScope(Dispatchers.IO).launch {
                val lis = async {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "RecoveryApp${File.separator}WhatsRecovery Statuses"
                    ).listFiles()
                }
                val it = lis.await()
                if (it != null) {
                    for (i in it.indices) {
                        val file: File = it[i]
                        val lUri = file.toUri()
                        val ext = file.extension
                        val type: Boolean = ext == "mp4"
                        saveMediaList.add(Status(file, file.name, file.path, type, lUri))
                    }
                }
                withContext(Dispatchers.Main) {
                    saveMediaList.reverse()
                    saveAdapter?.notifyDataSetChanged()

                }

            }

        }
    }

    private fun getSavedStatuses() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //statusList.clear()
            //pass video data to adapter list

            CoroutineScope(Dispatchers.IO).launch {
                val lis = async {
                    File(
                        mContext!!.getExternalFilesDir(File("RecoveryApp${File.separator}.Statuses").path)
                            ?.toString()!!
                    ).listFiles()
                }
                val it = lis.await()
                if (it != null) {
                    sorData(it)
                    for (i in it.indices) {
                        val file: File = it[i]
                        val lUri = file.toUri()
                        val ext = file.extension
                        val type: Boolean = ext == "mp4"
                        statusList.add(Status(file, file.name, file.path, type, lUri))
                    }

                }
                withContext(Dispatchers.Main) {
                    imageStatusAdapter.notifyDataSetChanged()

                }

            }


        } else {
            statusList.clear()
            //pass video data to adapter list
            CoroutineScope(Dispatchers.IO).launch {
                val lis = async {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "RecoveryApp${File.separator}.Statusess"
                    ).listFiles()
                }
                val it = lis.await()
                if (it != null) {
                    for (i in it.indices) {
                        val file: File = it[i]
                        val lUri = file.toUri()
                        val ext = file.extension
                        val type: Boolean = ext == "mp4"
                        statusList.add(Status(file, file.name, file.path, type, lUri))
                    }
                }
                withContext(Dispatchers.Main) {
                    imageStatusAdapter.notifyDataSetChanged()

                }

            }


        }
    }

    private fun sorData(array: Array<File>?): Array<String?> {
        val fileExtension = arrayOfNulls<String>(array!!.size)
        try {
            val sortedByDate: List<File> = array.toList()
            //Collections.reverse(sortedByDate)
            sortedByDate.mapIndexed { index, item ->
                fileExtension[index] = item.extension
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return fileExtension
    }

    suspend fun getStatus() {
        when {
            Common.STATUS_DIRECTORY.exists() -> {
                execute()
            }
            else -> {


            }
        }
    }

    fun checkPermissionForReadExtertalStorage(): Boolean {
        val result: Int =
            requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun execute() {
        val statusFiles: Array<File>
        if (Common.STATUS_DIRECTORY.exists() && checkPermissionForReadExtertalStorage()) {
            statusFiles = Common.STATUS_DIRECTORY.listFiles()
            if (statusFiles.isNotEmpty()) {

                videoList.clear()
                if (statusFiles.isNotEmpty()) {
                    Arrays.sort(statusFiles)
                    for (file in statusFiles) {
                        val status = Status(
                            file, file.name, file.absolutePath, file.name.endsWith(MP4), null
                        )
                        if (status.title!!.endsWith(".jpg") || status.title.endsWith(".mp4")) {
                            videoList.add(status)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    statusList.clear()
                    statusList.addAll(videoList)
                    imageStatusAdapter.notifyDataSetChanged()
                }
            } else {
                //handler?.post {
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(binding.root.context, binding.root.context.getString(R.string.no_status_file_found))
                    }
                }
                //}
            }
        }


        Noitems()
//        if (statusList.isEmpty()){
//            binding.status.visibility = View.GONE
//            binding.hideNofile.visibility = View.VISIBLE
//        }
//        binding.swipeRefreshLayout.isRefreshing = false
    }

    companion object {
        private const val TAG = "StatusFragment"
        fun RefreshDownloads() {
            instance!!.getDownloadedStatuses()

        }

        suspend fun refresh() {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {

                instance!!.getStatus()
            }
            instance!!.getDownloadedStatuses()


//            Noitems()
        }

        fun Noitems() {
            Handler(Looper.getMainLooper()).postDelayed({
                val relativeLayout: RelativeLayout
                val relativeLayoutDown: RelativeLayout
                val relativeLayoutStatus: RelativeLayout
                val relativeLayoutMain: RelativeLayout
                val relativeLayoutSt: LinearLayout
                var i: Int
                if (instance!!.saveAdapter?.itemCount == 0) {
                    relativeLayout = instance!!.binding.hideNoDownload
                    relativeLayoutDown = instance!!.binding.downloads
                    relativeLayout.visibility = View.VISIBLE
                    relativeLayoutDown.visibility = View.GONE
                } else {
                    instance?.let {inst->
                        inst.activity?.let {
                            inst.loadAppNative(it)
                        }
                    }
                    relativeLayout = instance!!.binding.hideNoDownload
                    relativeLayout.visibility = View.GONE
                    relativeLayoutDown = instance!!.binding.downloads
                    relativeLayoutDown.visibility = View.VISIBLE
                }
                if (instance!!.imageStatusAdapter.itemCount == 0) {
                    relativeLayoutStatus = instance!!.binding.statuses
                    relativeLayoutStatus.visibility = View.GONE
                } else {
                    instance?.let {inst->
                        inst.activity?.let {
                            inst.loadAppNative(it)
                        }
                    }
                    relativeLayoutStatus = instance!!.binding.hideNoDownload
                    relativeLayoutStatus.visibility = View.VISIBLE
                }
                if (instance!!.imageStatusAdapter.itemCount == 0 && instance!!.imageStatusAdapter.itemCount == 0) {
                    relativeLayoutMain = instance!!.binding.hidenofile
                    relativeLayoutSt = instance!!.binding.status
                    relativeLayoutSt.visibility = View.GONE
                    relativeLayoutMain.visibility = View.VISIBLE
                } else {
                    instance?.let {inst->
                        inst.activity?.let {
                            inst.loadAppNative(it)
                        }
                    }
                    relativeLayoutMain = instance!!.binding.hidenofile
                    relativeLayoutSt = instance!!.binding.status
                    relativeLayoutSt.visibility = View.VISIBLE
                    relativeLayoutMain.visibility = View.GONE
                }

                //                relativeLayout.setVisibility(i);
            }, 100)
        }

        var instance: StatusFragment? = null

    }


    private var nativeManger: NativeAdManager?= null
    fun loadAppNative(activity: Activity) {
        if(AdConst.IS_AD_SHOW){
            if (nativeManger == null && Common.isNetworkAvailable(activity)) {
                nativeManger = NativeAdManager(activity)
                val nativeId = binding.root.context.getString(R.string.native_ads)
                nativeManger?.showAndLoadNativeAd(
                    nativeId,
                    binding.tabNative,
                    binding.nativeTemplate,
                    object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(NativeAdManager.TAG_NATIVE, "Native Failure: $adError")
                            binding.tabNative.visibility = View.GONE
                            nativeManger = null
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d(NativeAdManager.TAG_NATIVE, "Native: ")
                            binding.nativeTemplate.visibility = View.VISIBLE
                        }
                    })
            }

            if (!(Common.isNetworkAvailable(activity)))
            {
                if (binding.tabNative.visibility==View.VISIBLE)
                {
                    if (binding.nativeTemplate.visibility==View.GONE)
                    {
                        binding.tabNative.visibility=View.GONE
                        nativeManger?.destroyNativeAd()
                        nativeManger=null
                    }
                }
            }
        }
        else
        {
            binding.tabNative.visibility=View.GONE
            nativeManger?.destroyNativeAd()
            nativeManger=null
        }
    }

    private val onNotice: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            suspend {
                getStatus()
            }
        }
    }


}