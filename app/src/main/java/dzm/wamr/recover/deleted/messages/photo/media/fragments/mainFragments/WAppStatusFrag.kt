package dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.*
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.AudioPlayerA
import dzm.wamr.recover.deleted.messages.photo.media.activities.MediaPreviewActivity
import dzm.wamr.recover.deleted.messages.photo.media.activities.SavedStatusActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.WAppStatusAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragWappStatusBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import java.io.File

class WAppStatusFrag : Fragment() {

    lateinit var binding:FragWappStatusBinding
    val statusImageList: ArrayList<StatusDataModel> = ArrayList()
    var selectedStatus:StatusDataModel? =null
    var mAdapter: WAppStatusAdapter? = null
    var prefManager: PrefManager? =null


    companion object {
        fun newInstance(): WAppStatusFrag {
            val fragment = WAppStatusFrag()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=DataBindingUtil.inflate(inflater,R.layout.frag_wapp_status, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.tvSaved.setOnClickListener(DebounceClickHandler(View.OnClickListener { v: View? ->
            if (AdConst.IS_AD_SHOW) {

                activity?.let {
                    InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(it,
                        object: AdCallback {
                            override fun onAdComplete(isCompleted: Boolean) {
                                InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,Const.AD_NEXT_REQUEST_DELAY)
                                AdConst.firstTourAd=false
                                moveToStatusSaver()

                            }
                        },true)
                }

            } else {
                moveToStatusSaver()
            }
        }))

        binding.sAccessBtn.setOnClickListener(DebounceClickHandler(View.OnClickListener { v: View? ->
            if (AdConst.IS_AD_SHOW) {

                if (AdConst.firstTourAd)
                {
                    activity?.let {
                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(it,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,Const.AD_NEXT_REQUEST_DELAY)
                                    AdConst.firstTourAd=false
                                    applyFolderAccess()

                                }
                            },true)
                    }
                }
                else
                {
                    applyFolderAccess()
                }

            } else {
                applyFolderAccess()
            }
        }))
    }
    private val launcherForOpenDocResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                try {
                    val uri = it.data
                    if (uri!=null)
                    {
                        binding.root.context.contentResolver.takePersistableUriPermission(
                            uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    prefManager?.setWATree(uri.toString())
                }catch (e:java.lang.Exception)
                {

                }
                populateGrid()

            }
        }
    }
    private fun applyFolderAccess() {
        if (Utils.appInstalledOrNot(binding.root.context, "com.whatsapp")) {
            val sm = binding.root.context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val statusDir = whatsupFolder
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
            MyAppOpenAd.isDialogClose=false
            launcherForOpenDocResult.launch(intent)
        } else {
            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.please_install_whatsapp_for_download_status))
        }
    }


    val whatsupFolder: String
        get() = if (File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
            ).isDirectory
        ) {
            "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        } else {
            "WhatsApp%2FMedia%2F.Statuses"
        }




    val resultRefreshCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {
                populateGrid()
            }
        }
    }
    }
    private fun moveToStatusSaver() {
        val callingIntent=Intent(binding.root.context, SavedStatusActivity::class.java)
        resultRefreshCallback.launch(callingIntent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }




    private fun initControl() {
        prefManager = PrefManager(binding.root.context)

        initAdapter()
    }




    override fun onResume() {
        super.onResume()
        populateGrid()
    }

    private fun initAdapter() {
        val layoutManager =GridLayoutManager(binding.root.context,2)
        layoutManager.orientation=GridLayoutManager.VERTICAL
        binding.myRecyclerView0.layoutManager = layoutManager
        mAdapter = WAppStatusAdapter(statusImageList, true,object :AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                selectedStatus=statusImageList.get(position)
                if (view.id==R.id.card_view)
                {
                    moveToStatusScreen(position,true,true)
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
        binding.myRecyclerView0.adapter = mAdapter
    }

    private fun moveToStatusScreen(position:Int,isWApp: Boolean,isComingFromGalleryOrStatus:Boolean) {
        if (statusImageList==null || statusImageList.isEmpty())
        {
            return
        }
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
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,Const.AD_NEXT_REQUEST_DELAY)
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
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        MyAppOpenAd.isDialogClose=false
        resultShareCallback.launch(share)
    }



    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if(menuVisible)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                if (prefManager?.getWATree()!=null && prefManager?.getWATree() != "") {
                    populateGrid()
                }


                activity?.let {
                    if (statusImageList.size>0)
                    {
                        loadAppNative(it)
                    }
                }

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
    fun checkIsNeedToRemoveAd(){
        activity?.let {
            loadAppNative(it)
        }
    }




    var async: loadDataAsync? = null
    fun populateGrid() {
        if (prefManager!!.getWATree()!=null && !(prefManager!!.getWATree()!!.isEmpty()))
        {
            async = loadDataAsync()
            async!!.execute()
        }
        else
        {
            binding.sAccessBtn.visibility=View.VISIBLE
            binding.isEmptyList.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (async != null) {
            async!!.cancel(true)
        }
    }

    open inner class loadDataAsync : AsyncTask<Void?, Void?, Void?>() {
        var allFiles: Array<DocumentFile>? = null
        override fun doInBackground(vararg params: Void?): Void? {
            allFiles = null
            val tempStatusImageList:ArrayList<StatusDataModel> = ArrayList()
            allFiles = fromSdcard
            allFiles?.let {allStatus->
                for (i in allStatus.indices) {
                    if (!allStatus[i].uri.toString().contains(".nomedia")) {
                        tempStatusImageList.add(
                            StatusDataModel(
                                allStatus[i].uri.toString(),
                                allStatus[i].name
                            )
                        )
                    }
                }
            }

            statusImageList.clear()
            statusImageList.addAll(tempStatusImageList)
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (statusImageList.isEmpty())
            {
                binding.loader.visibility = View.VISIBLE
            }
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            setupNoDataLayout()

        }
    }

    private fun setupNoDataLayout() {
        binding.loader.visibility = View.GONE
        if (statusImageList.isEmpty())
        {
            binding.sAccessBtn.visibility=View.GONE
            binding.isEmptyList.visibility = View.VISIBLE
            binding.myRecyclerView0.visibility = View.GONE
        }
        else
        {
            activity?.let {
                loadAppNative(it)
            }
            binding.sAccessBtn.visibility=View.GONE
            binding.isEmptyList.visibility = View.GONE
            binding.myRecyclerView0.visibility = View.VISIBLE
        }
        mAdapter?.notifyDataSetChanged()
    }

    private val fromSdcard: Array<DocumentFile>?
        private get() {
            val treeUri = prefManager!!.getWATree()
            return try {
                val fromTreeUri = DocumentFile.fromTreeUri(requireContext(), Uri.parse(treeUri))
                if (fromTreeUri != null && fromTreeUri.exists() && fromTreeUri.isDirectory && fromTreeUri.canRead() && fromTreeUri.canWrite()) {
                    fromTreeUri.listFiles()
                } else {
                    null
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
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

}