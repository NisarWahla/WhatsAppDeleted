package dzm.wamr.recover.deleted.messages.photo.media.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.AudioPlayerA
import dzm.wamr.recover.deleted.messages.photo.media.activities.MediaPreviewActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.WAppStatusAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentMediaItemBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.NewMediaFragment
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import kotlinx.coroutines.*
import java.io.File


class MediaItemF() : Fragment() {

    lateinit var binding:FragmentMediaItemBinding
    val statusImageList: ArrayList<StatusDataModel> = ArrayList()
    var selectedStatus: StatusDataModel? = null
    var adapter: WAppStatusAdapter? =null
    var isScrolling = false
    var permissionUtil: PermissionUtils? =null
    var childFolder=""

    companion object {

        fun newInstance(childFolder: String): MediaItemF {
            val fragment = MediaItemF()
            val args = Bundle()
            args.putString("childFolder",childFolder)

            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_media_item, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.tabStorageAccess.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            MyAppOpenAd.isDialogClose=false
            permissionUtil?.showStoragePermissionDailog(binding.root.context.getString(R.string.storage_permission_description))
        }))
    }


    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck = mutableListOf<String>()

            for (entry in result.entries) {
                val (key, value) = entry
                if (!value) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Utils.getPermissionStatus(requireActivity(), key))
                }
            }

            if (blockPermissionCheck.contains("blocked")) {
                Utils.showPermissionSetting(
                    binding.root.context,
                    binding.root.context.getString(R.string.storage_permission_description)
                )
            } else if (allPermissionClear) {
                populateGrid(childFolder)
            }
            MyAppOpenAd.isDialogClose=true
            MyAppOpenAd.isAppInBackground=false
        }
    private fun initControl() {
        arguments?.let {
            childFolder=it.getString("childFolder","")
        }
        permissionUtil= PermissionUtils(requireActivity(),storagePermissionResult)
        initAdapter()
    }





    private fun initAdapter() {
        val layoutManager = GridLayoutManager(binding.root.context,2)
        layoutManager.orientation= GridLayoutManager.VERTICAL
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        adapter = WAppStatusAdapter(statusImageList, false, object: AdapterClicklistener {
            override fun onItemClick(view: View, position: Int) {
                selectedStatus=statusImageList.get(position)
                if (view.id==R.id.card_view)
                {
                    moveToStatusScreen(position,true,false)
                }
                else
                    if (view.id==R.id.downloadIV)
                    {
                        downloadStatus()
                    }
                    else
                        if (view.id==R.id.share_icon)
                        {
                            shareStatus(true)
                        }
            }
        })

        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            }
        })
    }

    private fun moveToStatusScreen(position:Int,isWApp: Boolean,isComingFromGalleryOrStatus:Boolean) {
        val folderPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.root.context.getExternalFilesDir("SavedStatus").toString()
        } else {
            Environment.getExternalStorageDirectory().absolutePath + "/SavedStatus"
        }

        if (AdConst.IS_AD_SHOW) {

            activity?.let {

                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(it,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,
                                Const.AD_NEXT_REQUEST_DELAY)

                            AdConst.firstTourAd=false
                            moveToPreview(
                                statusImageList,
                                position,
                                isWApp,
                                folderPath,
                                isComingFromGalleryOrStatus,
                                "status"
                            )
                        }
                    },true)
            }


        } else {
            moveToPreview(
                statusImageList,
                position,
                isWApp,
                folderPath,
                isComingFromGalleryOrStatus,
                "status"
            )
        }
    }


    private fun downloadStatus() {
        selectedStatus?.let {
            val selectedFilePath =it.filePath
            val selectedFileName =it.fileName

            Log.d(Const.tag,"pathWithName: ${selectedFilePath}")
            val filestatus=Utils.copyFileFromContentUri(binding.root.context,selectedFilePath,
                Utils.getPrivateAppFolder(binding.root.context,".MySavedStatus"),selectedFileName)
            if (filestatus==Utils.FileExistence.SRC_NOT_EXIST)
            {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.no_status_file_found))
            } else  if (filestatus==Utils.FileExistence.COPY_FILE_ALREADY_EXIST)
            {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.image_already_saved))
            } else  if (filestatus==Utils.FileExistence.EXCEPTION_FACE)
            {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.media_scan_failed))
            } else
            {
                if (AdConst.IS_AD_SHOW) {
                    activity?.let {act->
                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(act,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(act,
                                        Const.AD_NEXT_REQUEST_DELAY)

                                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved))
                                }
                            },true)
                    }
                } else {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved))
                }

            }

        }
    }

    private fun shareStatus(isWApp: Boolean) {
        selectedStatus?.let {
            val finalPath = Utils.getStatusDir(binding.root.context).absolutePath
            val pathWithName = finalPath + File.separator + File(it.filePath).name
            Log.i("WAMR TAG", "onBindViewHolder: exist$pathWithName")
            shareFile(binding.root.context, isWApp, it.filePath)
        }

    }


    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }
    fun shareFile(context: Context, isVideo: Boolean, path: String) {
        val share = Intent()
        share.action = Intent.ACTION_SEND
        if (isVideo) share.type = "Video/*" else share.type = "image/*"
        val uri: Uri = if (path.startsWith("content")) {
            Uri.parse(path)
        } else {
            FileProvider.getUriForFile(
                context, context.applicationContext.packageName + ".provider", File(path)
            )
        }
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.putExtra(Intent.EXTRA_STREAM, uri)
        MyAppOpenAd.isDialogClose=false
        resultShareCallback.launch(share)
    }






    @SuppressLint("NotifyDataSetChanged")
    private fun populateGrid(childFolder: String) {
        //binding.sAccessBtn.visibility = View.GONE
        if (context==null)
        {
         return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val files = async {
                val destinationDirectory =
                    File(binding.root.context.getExternalFilesDir(null), childFolder)
                if (!destinationDirectory.exists()) {
                    destinationDirectory.mkdirs()
                    val files = destinationDirectory.listFiles()
                    files?.sortByDescending { it.lastModified() }
                    files?.toList()
                } else {
                    val files = destinationDirectory.listFiles()
                    files?.sortByDescending { it.lastModified() }
                    files?.toList()
                }
            }
            statusImageList.clear()
            val allFiles = files.await()
            allFiles?.forEachIndexed { _, documentFile ->

                // file path name
                val path=documentFile.path
                val pathName=childFolder
                if (pathName.contains("Images"))
                {
                    if(Utils.isImageFileExtention(path))
                    {
                        statusImageList.add(StatusDataModel(documentFile.path, documentFile.name))
                    }

                } else if (pathName.contains("Animated Gifs"))
                {
                    if(Utils.isGifFileExtention(path))
                    {
                        statusImageList.add(StatusDataModel(documentFile.path, documentFile.name))
                    }

                } else if (pathName.contains("Video"))
                {
                    if(Utils.isVideoFileExtention(path))
                    {
                        statusImageList.add(StatusDataModel(documentFile.path, documentFile.name))
                    }
                } else if (pathName.contains("Audio"))
                {
                    if(Utils.isAudioFileExtention(path))
                    {
                        statusImageList.add(StatusDataModel(documentFile.path, documentFile.name))
                    }
                }
            }
            withContext(Dispatchers.Main) {
                updateNoDataListener()
            }
        }
    }


    fun updateNoDataListener() {
        if (statusImageList.isEmpty())
        {
            binding.relativeHide.visibility=View.VISIBLE
            binding.recyclerView.visibility=View.GONE

            permissionUtil?.let {permission->
                if (permission.isStoragePermissionGranted())
                {
                    binding.tabStorageAccess.visibility=View.GONE
                    binding.tabEmptyList.visibility=View.VISIBLE
                    val pathName=childFolder
                    if (pathName.contains("Images"))
                    {
                        binding.mygifviewtext.text=binding.root.context.getText(R.string.no_image_found)
                    } else if (pathName.contains("Animated Gifs"))
                    {
                        binding.mygifviewtext.text=binding.root.context.getText(R.string.no_gif_found)
                    } else if (pathName.contains("Video"))
                    {
                        binding.mygifviewtext.text=binding.root.context.getText(R.string.no_video_found)
                    } else if (pathName.contains("Audio"))
                    {
                        binding.mygifviewtext.text=binding.root.context.getText(R.string.no_audio_found)
                    }
                }
                else
                {
                    binding.tabStorageAccess.visibility=View.VISIBLE
                    binding.tabEmptyList.visibility=View.GONE
                }
            }

        }
        else
        {
            activity?.let {
                loadAppNative(it)
            }
            binding.relativeHide.visibility=View.GONE
            binding.recyclerView.visibility=View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
    }



    private val dataUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NewMediaFragment.ACTION_NEW_MEDIA) {
                // Handle the broadcasted data here
                ReLoadList()
            }
        }
    }

    fun ReLoadList()
    {
        if (menuVisible)
        {
            populateGrid(childFolder)
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(NewMediaFragment.ACTION_NEW_MEDIA)
        activity?.registerReceiver(dataUpdateReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(dataUpdateReceiver)
    }


    val resultRefreshCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {
                ReLoadList()
            }
        }
    }
    }

    fun moveToPreview(
        statusDataList: ArrayList<StatusDataModel>,
        position: Int,
        isWApp: Boolean,
        folderPath: String,
        isComingFromGalleryOrStatus: Boolean,
        status: String
    ) {
        if (statusDataList[position].fileName.contains("AUD")) {
            val intent = Intent(context, AudioPlayerA::class.java)
            intent.putExtra("audioList",statusDataList)
            intent.putExtra("currentPosition",position)
            resultRefreshCallback.launch(intent)
            activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        } else {
            val intent = Intent(activity, MediaPreviewActivity::class.java)
            intent.putParcelableArrayListExtra("images", statusDataList)
            intent.putExtra("position", position)
            intent.putExtra("statusdownload", status)
            intent.putExtra("isWApp", isWApp)
            intent.putExtra("folderpath", folderPath)
            intent.putExtra("isComingFromGalleryOrStatus", isComingFromGalleryOrStatus)
            resultRefreshCallback.launch(intent)
            activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }





    var menuVisible:Boolean=false
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        this.menuVisible=menuVisible
        if(menuVisible)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                populateGrid(childFolder)
            },200)
        }
    }



    private var nativeManger: NativeAdManager?= null
    private fun loadAppNative(activity: Activity) {
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

}