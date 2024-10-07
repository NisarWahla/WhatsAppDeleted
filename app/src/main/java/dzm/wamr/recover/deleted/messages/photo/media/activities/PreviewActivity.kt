package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.FullscreenImageAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityPreviewBinding
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.download
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.isVideoFile
import java.io.File

class PreviewActivity : AppCompatLocaleActivity() {
    val imageList: ArrayList<StatusDataModel> = ArrayList()
    var position = 0
    var fullscreenImageAdapter: FullscreenImageAdapter? = null
    var statusdownload: String? = null
    var folderPath: String? = null
    lateinit var binding:ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_preview)
        val botmparam = LayManager.setLinParams(binding.root.context, 1080, 307)
        binding.bottomLay.layoutParams = botmparam
        val btnParam = LayManager.setLinParams(binding.root.context, 300, 100)
        binding.menuSave.layoutParams = btnParam
        binding.menuShare.layoutParams = btnParam
        binding.menuDelete.layoutParams = btnParam
        val dataList:ArrayList<StatusDataModel> = intent.getParcelableArrayListExtra("images")!!
        if (dataList!=null)
        {
            imageList.clear()
            imageList.addAll(dataList)
        }
        position = intent.getIntExtra("position", 0)
        statusdownload = intent.getStringExtra("statusdownload")
        folderPath = intent.getStringExtra("folderpath")
        if (statusdownload == "download") {
            binding.bottomLay.weightSum = 2.0f
            binding.menuSave.visibility = View.GONE
        } else {
            binding.bottomLay.weightSum = 3.0f
            binding.menuSave.visibility = View.VISIBLE
        }
        Log.e(Const.tag, "" + imageList[0].filePath)
        fullscreenImageAdapter = FullscreenImageAdapter( imageList)
        binding.viewPager.adapter = fullscreenImageAdapter
        binding.viewPager.currentItem = position
        binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                /*  if (position % 2 == 0) {
                    AdManger.showInterstitial(PreviewActivity.this, PreviewActivity.this, new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);

                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                        }
                    });
                }*/
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.menuSave.setOnClickListener(clickListener)
        binding.menuShare.setOnClickListener(clickListener)
        binding.menuDelete.setOnClickListener(clickListener)
        binding.backIV.setOnClickListener(clickListener)
        loadAppBanner(this@PreviewActivity)

    }

    private var bannerManger: BannerManager?= null
    private fun loadAppBanner(activity: Activity) {
        if (bannerManger==null)
        {
            bannerManger= BannerManager(activity)
            val bannerId=binding.root.context.getString(R.string.admob_banner_id)
            bannerManger?.showAndLoadBannerAd(bannerId,binding.tabNative,object: AdListener(){
                override fun onAdFailedToLoad(adError : LoadAdError) {
                    Log.d(BannerManager.TAG_BANNER,"Banner Failure: $adError")
                    binding.tabNative.visibility= View.GONE
                    bannerManger=null
                }
            })
        }

        if (Common.isNetworkAvailable(activity))
        {
            if (AdConst.IS_AD_SHOW)
            {
                if (binding.tabNative.visibility==View.GONE)
                {
                    if (bannerManger!=null)
                    {
                        bannerManger=null
                        loadAppBanner(activity)
                    }
                }
            }
        }
    }


    private val clickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.backIV -> {
                onBackPressed()
            }
            R.id.menu_save -> {
                if (imageList!!.size > 0) {
                    if (AdConst.IS_AD_SHOW) {
                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@PreviewActivity,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@PreviewActivity,
                                        Const.AD_NEXT_REQUEST_DELAY)

                                    try {
                                        download(
                                            this@PreviewActivity,
                                            imageList[binding.viewPager.currentItem].filePath)
                                        Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved_successfully))

                                    } catch (e: Exception) {
                                        Utils.showToast(binding.root.context,binding.root.context.getString(R.string.sorry_msg))
                                    }
                                }
                            },true)

                    } else {
                        try {
                            download(
                                this@PreviewActivity,
                                imageList[binding.viewPager.currentItem].filePath
                            )
                            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved_successfully))
                        } catch (e: Exception) {
                            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.sorry_msg))
                        }
                    }
                } else {
                    onBackPressed()
                }
            }
            R.id.menu_share -> {
                if (imageList!!.size > 0) {
                    shareFile(
                        binding.root.context,
                        isVideoFile(
                            binding.root.context,
                            imageList!![binding.viewPager.currentItem].filePath
                        ),
                        imageList!![binding.viewPager.currentItem].filePath
                    )
                } else {
                    onBackPressed()
                }
            }
            R.id.menu_delete -> {
                if (imageList!!.size > 0) {
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
                    fileName.text = imageList!![binding.viewPager.currentItem].fileName
                    noBtn.setOnClickListener { v1: View? -> dialog.dismiss() }
                    yesBtn.setOnClickListener { v1: View? ->
                        dialog.dismiss()
                        val currentItem = 0
                        if (statusdownload == "download") {
                            val fPath = imageList!![binding.viewPager.currentItem].filePath
                            val file = File(fPath)
                            if (file.exists()) {
                                val del = file.delete()
                                delete(currentItem)
                            }
                        } else {
                            val fromTreeUri = DocumentFile.fromSingleUri(
                                binding.root.context, Uri.parse(
                                    imageList!![binding.viewPager.currentItem].filePath
                                )
                            )
                            if (fromTreeUri!!.exists()) {
                                val del = fromTreeUri.delete()
                                delete(currentItem)
                            }
                        }
                    }
                    dialog.show()
                } else {
                    onBackPressed()
                }
            }
            else -> {}
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




    fun delete(currentItem: Int) {
        var currentItem = currentItem
        if (imageList!!.size > 0 && binding.viewPager.currentItem < imageList!!.size) {
            currentItem = binding.viewPager.currentItem
        }
        imageList!!.removeAt(binding.viewPager.currentItem)
        fullscreenImageAdapter = FullscreenImageAdapter( imageList)
        binding.viewPager.adapter = fullscreenImageAdapter
        val intent = Intent()
        setResult(10, intent)
        if (imageList!!.size > 0) {
            binding.viewPager.currentItem = currentItem
        } else {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }




}