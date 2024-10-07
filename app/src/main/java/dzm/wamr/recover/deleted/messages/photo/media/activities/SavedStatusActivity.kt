package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.*
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.StatusDownloadAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivitySavedStatusBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File
import java.util.*

class SavedStatusActivity : AppCompatLocaleActivity() {
    var downloadImageList = ArrayList<StatusDataModel>()
    var adapter: StatusDownloadAdapter? = null

    lateinit var binding:ActivitySavedStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_saved_status)
        initControl()
        actionControl()

    }

    private fun actionControl() {
        binding.backIV.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
    }

    private fun initControl() {
        setupAdapter()
        loadMedia()
    }

    private fun setupAdapter() {
        val layoutManager = GridLayoutManager(binding.root.context, 3)
        layoutManager.orientation=GridLayoutManager.VERTICAL
        binding.recyclerView.layoutManager = layoutManager
        adapter = StatusDownloadAdapter(
            downloadImageList,object :AdapterClicklistener{
                override fun onItemClick(view: View, position: Int) {
                    if(view.id==R.id.card_view)
                    {
                        moveToPreviewWithAdLogic(position)
                    }else if(view.id==R.id.shareIV)
                    {
                        ShareStatus(downloadImageList.get(position).filePath)
                    } else if(view.id==R.id.deleteIV)
                    {
                        deleteStatus(position)
                    }
                }
            })
        binding.recyclerView.adapter = adapter
    }

    override fun onBackPressed() {
        if (AdConst.IS_AD_SHOW) {
            if (MyApp.showEvenAd == 1) {
                MyApp.showEvenAd = 0

                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@SavedStatusActivity,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SavedStatusActivity,
                                Const.AD_NEXT_REQUEST_DELAY)

                            moveWithResult()
                        }
                    },true)

            } else {
                MyApp.showEvenAd = 1
                moveWithResult()
            }
        } else {
            moveWithResult()
        }
    }

    private fun moveWithResult() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }

    private var nativeManger: NativeAdManager?= null
    private fun loadAppNative(activity: Activity) {
        if (AdConst.IS_AD_SHOW){
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




    fun loadMedia() {
        val file =File(Utils.getPrivateAppFolder(binding.root.context,".MySavedStatus"))
        displayfiles(file)
    }

    fun displayfiles(file: File) {
        val listfilemedia = dirListByAscendingDate(file)
        var i = 0

        listfilemedia?.let {mediaFile->
            downloadImageList.clear()
            while (i < mediaFile.size) {
                downloadImageList.add(
                    StatusDataModel(
                        mediaFile[i].absolutePath,
                        mediaFile[i].name
                    )
                )
                i++
            }
        }
        setupNoDataLayout()
    }

    private fun setupNoDataLayout() {
        if (downloadImageList.isEmpty())
        {
            binding.isEmptyList.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
        else
        {
            loadAppNative(this@SavedStatusActivity)
            binding.isEmptyList.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
    }

    fun deleteStatus(position: Int) {
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.delete_confirmation_dialog)
        dialog.setCancelable(true)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val yesBtn = dialog.findViewById<TextView>(R.id.txt_yes)
        val noBtn = dialog.findViewById<TextView>(R.id.txt_no)
        val fileName = dialog.findViewById<TextView>(R.id.file_name)
        fileName.isSelected = true
        fileName.text = File(downloadImageList[position].filePath).name
        noBtn.setOnClickListener { v: View? -> dialog.dismiss() }
        yesBtn.setOnClickListener { v: View? ->
            dialog.dismiss()
            val file = File(downloadImageList[position].filePath)
            if (file.exists()) {
                file.delete()
                downloadImageList.removeAt(position)
                if (downloadImageList.isEmpty()) {
                    binding.isEmptyList.visibility = View.VISIBLE
                } else {
                    binding.isEmptyList.visibility = View.GONE
                }
            }
            setupNoDataLayout()
        }
        dialog.show()
    }


    private fun moveToPreviewWithAdLogic(position: Int) {
        if (AdConst.IS_AD_SHOW) {
            InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@SavedStatusActivity,
                object: AdCallback {
                    override fun onAdComplete(isCompleted: Boolean) {
                        InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SavedStatusActivity,Const.AD_NEXT_REQUEST_DELAY)
                        AdConst.firstTourAd=false
                        moveToPreview(position)

                    }
                },true)

        } else {
            moveToPreview(position)
        }
    }
    val resultRefreshCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {
                loadMedia()
            }
        }
    }
    }
    private fun moveToPreview(position: Int) {
        val intent = Intent(binding.root.context, PreviewActivity::class.java)
        intent.putParcelableArrayListExtra("images", downloadImageList)
        intent.putExtra("position", position)
        intent.putExtra("statusdownload", "download")
        resultRefreshCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }



    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }

    fun ShareStatus(filepath: String?) {
        val fileToShare = File(filepath)
        if (Utils.isImageFile(filepath)) {
            val share = Intent(Intent.ACTION_SEND)
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            share.type = "image/*"
            val photoURI = FileProvider.getUriForFile(
                binding.root.context,
                binding.root.context.packageName + ".provider",
                fileToShare
            )
            share.putExtra(
                Intent.EXTRA_STREAM, photoURI
            )
            MyAppOpenAd.isDialogClose=false
            resultShareCallback.launch(Intent.createChooser(share, binding.root.context.getString(R.string.share_via)))
        } else if (Utils.isVideoFile(filepath)) {
            val videoURI = FileProvider.getUriForFile(
                binding.root.context,
                binding.root.context.packageName + ".provider",
                fileToShare
            )
            val videoshare = Intent(Intent.ACTION_SEND)
            videoshare.type = "*/*"
            videoshare.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            videoshare.putExtra(Intent.EXTRA_STREAM, videoURI)
            MyAppOpenAd.isDialogClose=false
            resultShareCallback.launch(videoshare)
        }
    }


    companion object {
        fun dirListByAscendingDate(folder: File): Array<File>? {
            if (!folder.isDirectory) {
                return null
            }
            val sortedByDate = folder.listFiles()
            if (sortedByDate == null || sortedByDate.size <= 1) {
                return sortedByDate
            }
            Arrays.sort(sortedByDate, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
            return sortedByDate
        }
    }
}