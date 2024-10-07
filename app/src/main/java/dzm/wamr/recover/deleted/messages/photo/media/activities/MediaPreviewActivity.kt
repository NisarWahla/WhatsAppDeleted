package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.viewpager.widget.ViewPager
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.FullscreenImageAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityMediaPreviewBinding
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.getStatusDir
import java.io.File

class MediaPreviewActivity : AppCompatLocaleActivity() {
    val imageList: ArrayList<StatusDataModel> = ArrayList()
    var position = 0
    var fullscreenImageAdapter: FullscreenImageAdapter? = null
    var statusdownload: String? = null
    var folderPath: String? = null
    private var isComingFromGalleryOrStatus: Boolean = false
    lateinit var binding:ActivityMediaPreviewBinding
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_media_preview)

        binding.back.setOnClickListener(DebounceClickHandler(View.OnClickListener {  onBackPressed() }))
        loadAppBanner(this@MediaPreviewActivity)
        val datalist:ArrayList<StatusDataModel> = intent.getParcelableArrayListExtra("images")!!
        if (datalist!=null)
        {
            imageList.clear()
            imageList.addAll(datalist)
        }
        position = intent.getIntExtra("position", 0)
        statusdownload = intent.getStringExtra("statusdownload")
        folderPath = intent.getStringExtra("folderpath")
        isComingFromGalleryOrStatus = intent.getBooleanExtra("isComingFromGalleryOrStatus", false)
        if (isComingFromGalleryOrStatus) {
            binding.downloadIcon.visibility = View.VISIBLE
            binding.setWall.visibility = View.GONE
            binding.deleteLayout.visibility = View.GONE
            if (Utils.getBack(
                    imageList[position].filePath,
                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                )!!.isNotEmpty()
            ) {
                binding.setWall.visibility = View.GONE
            } else {
                binding.setWall.visibility = View.VISIBLE
            }
        } else {
            binding.downloadIcon.visibility = View.GONE
            binding.deleteLayout.visibility = View.VISIBLE
            binding.setWall.visibility = View.VISIBLE
            if (Utils.getBack(
                    imageList[position].filePath,
                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                )!!.isNotEmpty()
            ) {
                binding.setWall.visibility = View.GONE
            } else {
                binding.setWall.visibility = View.VISIBLE
            }
        }

        /*if (statusdownload.equals("download")) {
            bottomLay.setWeightSum(2.0f);
            menu_save.setVisibility(View.GONE);
        } else {

            bottomLay.setWeightSum(3.0f);
            menu_save.setVisibility(View.VISIBLE);
        }*/

        //AdManger.loadInterstial(applicationContext)
        fullscreenImageAdapter = FullscreenImageAdapter(imageList)
        binding.viewPager.adapter = fullscreenImageAdapter
        binding.viewPager.currentItem = position
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {

                /*   if (position % 2 == 0) {
                       if (AdConst.IS_AD_SHOW) {
                           AdManger.showInterstitial(
                               this@MediaPreviewActivity,
                               this@MediaPreviewActivity,
                               object : FullScreenContentCallback() {
                                   override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                       super.onAdFailedToShowFullScreenContent(adError)
                                   }

                                   override fun onAdShowedFullScreenContent() {
                                       super.onAdShowedFullScreenContent()
                                   }

                                   override fun onAdDismissedFullScreenContent() {
                                       super.onAdDismissedFullScreenContent()
                                   }

                                   override fun onAdImpression() {
                                       super.onAdImpression()
                                   }

                                   override fun onAdClicked() {
                                       super.onAdClicked()
                                   }
                               })
                       }
                   }*/
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.setWall.setOnClickListener(clickListener)
        binding.shareImg.setOnClickListener(clickListener)
        binding.deleteLayout.setOnClickListener(clickListener)
        binding.downloadIcon.setOnClickListener(clickListener)
    }


    private val clickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.setWall -> {
                val bottomSheetDialog = BottomSheetDialog(binding.root.context)
                val view = LayoutInflater.from(binding.root.context).inflate(R.layout.set_wallpaper_dialog, null)
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
                        setWallpaper(
                            binding.root.context,
                            imageList[binding.viewPager.currentItem].filePath,
                            0,
                            view
                        )
                    } else {
                        setWallpaper(
                            binding.root.context,
                            imageList[binding.viewPager.currentItem].filePath,
                            0,
                            view
                        )
                    }
                    bottomSheetDialog.dismiss()
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    setLock.visibility = View.GONE
                } else {
                    setLock.setOnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            setWallpaper(
                                binding.root.context,
                                imageList[binding.viewPager.currentItem].filePath,
                                1,
                                view
                            )
                        } else {
                            setWallpaper(
                                binding.root.context,
                                imageList[binding.viewPager.currentItem].filePath,
                                1,
                                view
                            )
                        }

                        bottomSheetDialog.dismiss()
                    }
                }
                setBoth.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setWallpaper(
                            binding.root.context,
                            imageList[binding.viewPager.currentItem].filePath,
                            2,
                            view
                        )
                    } else {
                        setWallpaper(
                            binding.root.context,
                            imageList[binding.viewPager.currentItem].filePath,
                            2,
                            view
                        )
                    }

                    bottomSheetDialog.dismiss()
                }
            }
            R.id.shareImg -> if (imageList.size > 0) {
                shareFile(
                    binding.root.context,
                    Utils.isVideoFile(
                        binding.root.context,
                        imageList[binding.viewPager.currentItem].filePath
                    ),
                    imageList[binding.viewPager.currentItem].filePath
                )
            } else {
                onBackPressed()
            }
            R.id.downloadIcon -> {
                downloadData()
            }
            R.id.delete_layout -> if (imageList.size > 0) {
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
                fileName.text = imageList[binding.viewPager.currentItem].fileName
                yesBtn.setOnClickListener {
                    dialog.dismiss()
                    val currentItem = 0

                    if (statusdownload == "download") {
                        val fPath = imageList[binding.viewPager.currentItem].filePath
                        val file = File(fPath)
                        if (file.exists()) {
                            //StatusActivity.Companion.setStatusDeleted(true)
                            val del = file.delete()
                            delete(currentItem)
                        }
                    } else {
                        val fromTreeUri = DocumentFile.fromSingleUri(
                            binding.root.context, Uri.parse(
                                imageList[binding.viewPager.currentItem].filePath
                            )
                        )
                        if (fromTreeUri!!.exists()) {
                            //StatusActivity.Companion.setStatusDeleted(true)
                            val del = fromTreeUri.delete()
                            delete(currentItem)
                        } else {
                            val mFile = File(imageList[binding.viewPager.currentItem].filePath)
                            if (mFile.exists()) {
                                //StatusActivity.Companion.setStatusDeleted(true)
                                mFile.delete()
                                delete(currentItem)
                            }
                        }
                    }
                    if (imageList.size == 0) {
                        onBackPressed()
                    }
                }
                noBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                onBackPressed()
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
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.putExtra(Intent.EXTRA_STREAM, uri)
        MyAppOpenAd.isDialogClose=false
        resultShareCallback.launch(share)
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



    fun delete(currentItem: Int) {
        var currentItem = currentItem
        if (imageList.size > 0 && binding.viewPager.currentItem < imageList.size) {
            currentItem = binding.viewPager.currentItem
        }
        imageList.removeAt(binding.viewPager.currentItem)
        fullscreenImageAdapter = FullscreenImageAdapter(imageList)
        binding.viewPager.adapter = fullscreenImageAdapter
        val intent = Intent()
        setResult(10, intent)
        if (imageList.size > 0) {
            binding.viewPager.currentItem = currentItem
        } else {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun downloadData() {
        if (imageList.size > 0 && imageList[binding.viewPager.currentItem].filePath!=null) {
            val selectedFilePath =imageList[binding.viewPager.currentItem].filePath
            val selectedFileName =imageList[binding.viewPager.currentItem].fileName

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
                    InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@MediaPreviewActivity,
                        object: AdCallback {
                            override fun onAdComplete(isCompleted: Boolean) {
                                InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@MediaPreviewActivity,
                                    Const.AD_NEXT_REQUEST_DELAY)

                                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved))
                            }
                        },true)
                } else {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved))
                }

            }

        } else {
            onBackPressed()
        }
    }

    private fun setWallpaper(context: Context, body: String, check: Int, view: View): Boolean {
//        initProgressDialog(view)
        Glide.with(context).asBitmap().load(body).override(512, 512).into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(
                bitmap: Bitmap, transition: Transition<in Bitmap>?
            ) {
                val manager = WallpaperManager.getInstance(context)
                if (check == 0) {
                    try {
                        manager.setBitmap(bitmap)
//                            progressDialog!!.dismiss()
                        Utils.showToast(binding.root.context,binding.root.context.getString(R.string.set_home))
                    } catch (ex: Exception) {
                        ex.printStackTrace()
//                            progressDialog!!.dismiss()
                    }
                } else if (check == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
//                                progressDialog!!.dismiss()
                            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.set_lock))
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
                        Utils.showToast(binding.root.context,binding.root.context.getString(R.string.set_both))
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

    override fun onBackPressed() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }
}