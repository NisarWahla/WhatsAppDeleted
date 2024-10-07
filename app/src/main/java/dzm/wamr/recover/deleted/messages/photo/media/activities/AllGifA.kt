package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloader.*
import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.core.network.api.CompletionHandler
import com.giphy.sdk.core.network.api.GPHApi
import com.giphy.sdk.core.network.api.GPHApiClient
import com.giphy.sdk.core.network.response.ListMediaResponse
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.GifAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityAllGifBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.hideKeyboard
import java.io.File


class AllGifA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityAllGifBinding
    var adapter: GifAdapter? =null
    var dataList:ArrayList<String> = ArrayList()
    var searchList:ArrayList<String> = ArrayList()
    var client: GPHApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_all_gif)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
        binding.etSearch.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 1000) { text ->
                applyAdapterFilter("${text}")
            }
        )
        binding.ivCloseSearch.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            clearSearch(true)
        }))

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearSearch()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun applyAdapterFilter(s: String) {
        if (s.isEmpty())
        {
            if (searchList.isEmpty())
            {
                getTrendingGif()
            }
            else
            {
                dataList.clear()
                dataList.addAll(searchList)
                adapter?.notifyDataSetChanged()
            }
        }
        else
        {
            searchGif(s)
        }

    }
    fun searchGif(search: String) {
        if (Common.isNetworkAvailable(binding.root.context))
        {
            binding.progressBar.visibility=View.VISIBLE
            client?.search(search, MediaType.gif, null, null, null, null, object :
                CompletionHandler<ListMediaResponse> {
                override fun onComplete(result: ListMediaResponse?, e: Throwable?) {
                    if (result == null) {
                        setupNoDataResult()
                    } else {
                        if (result.data != null) {
                            dataList.clear()
                            for (gif in result.data) {
                                dataList.add("${gif.id}")
                            }
                            setupNoDataResult()
                        } else {
                            setupNoDataResult()
                        }
                    }
                }
            })
        }
        else
        {
            setupNoDataResult()
        }
    }


    private fun clearSearch(isTextClear:Boolean=false) {
        if (isTextClear)
        {
            binding.etSearch.setText("")
        }
        binding.root.context?.hideKeyboard(binding.etSearch)
        binding.etSearch.clearFocus()
    }

    private fun initControl() {
        client = GPHApiClient(binding.root.context.getString(R.string.gif_api_key))
        setupAdapter()
    }


    var avoidMultipleSelection=true
    private fun setupAdapter() {
        val layoutManager= GridLayoutManager(binding.root.context,2)
        layoutManager.orientation= GridLayoutManager.VERTICAL
        binding.recyclerView.layoutManager=layoutManager
        adapter= GifAdapter(dataList, object : AdapterClicklistener {
            override fun onItemClick(view: View, position: Int) {
                val selectedItem=dataList.get(position)
                if (avoidMultipleSelection)
                {
                    avoidMultipleSelection=false
                    if (view.id==R.id.ivShare)
                    {
                        checkGifExistenceAndShare(selectedItem)
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    avoidMultipleSelection=true
                },300)
            }
        })
        binding.recyclerView.adapter=adapter


        Handler(Looper.getMainLooper()).postDelayed({
            getTrendingGif()
        },200)
    }

    private fun checkGifExistenceAndShare(gifId: String) {
        val gifName=gifId+".gif"
        val gifPath=Utils.getPrivateAppFolder(binding.root.context)+"/${Const.GIF_DIRECTORY_NAME}/"
        val gifDirectory=File(gifPath)
        Log.d(Const.tag,"Directory: ${gifDirectory.absolutePath}")
        if (!(gifDirectory.exists()))
        {
            gifDirectory.mkdirs()
        }
        val file=File(gifPath,gifName)
        Log.d(Const.tag,"File: ${file.absolutePath}")
        if (file.exists())
        {
            shareGifFile(file)
        }
        else
        {
            if (Common.isNetworkAvailable(binding.root.context))
            {
                downloadAndShareGif(gifId)
            }
            else
            {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_check_your_internet_connection))
            }
        }
    }

    private fun downloadAndShareGif(gifId: String) {
        val fileName=gifId+".gif"
        val dirPath=Utils.getPrivateAppFolder(binding.root.context)+"/${Const.GIF_DIRECTORY_NAME}/"
        val url=Utils.convertIntoGif(gifId)
        PRDownloader.download(url, dirPath, fileName)
            .build()
            .start(object:OnDownloadListener{
                override fun onDownloadComplete() {
                    val file=File(dirPath,fileName)
                    shareGifFile(file)
                }

                override fun onError(error: com.downloader.Error?) {
                    Log.d(Const.tag,"DownloadException: ${error}")
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.gif_share_failed_try_again))
                }
            })
    }

    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }
    private fun shareGifFile(file: File) {
        val shareIntent = Intent()
        val uri: Uri = FileProvider.getUriForFile(binding.root.context,
            binding.root.context.getPackageName() + ".provider", file)
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/*"
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        val chooserIntent = Intent.createChooser(shareIntent, binding.root.context.getString(R.string.share_via))
        MyAppOpenAd.isDialogClose=false
        resultShareCallback.launch(chooserIntent)
    }

    private fun getTrendingGif() {

        if (Common.isNetworkAvailable(binding.root.context))
        {
            binding.progressBar.visibility=View.VISIBLE
            client?.trending(MediaType.gif, null, null, null, object :
                CompletionHandler<ListMediaResponse> {
                override fun onComplete(result: ListMediaResponse?, e: Throwable?) {
                    if (result == null) {
                        setupNoDataResult()
                    } else {
                        if (result.data != null) {
                            dataList.clear()
                            searchList.clear()
                            for (gif in result.data) {
                                dataList.add("${gif.id}")
                                searchList.add("${gif.id}")
                            }
                            setupNoDataResult()
                        } else {
                            setupNoDataResult()
                        }
                    }
                }
            })
        }
        else
        {
            setupNoDataResult()
        }

    }

    private fun setupNoDataResult() {
        binding.progressBar.visibility = View.GONE
        if (dataList.isEmpty())
        {
            binding.tabNoData.visibility=View.VISIBLE
            binding.recyclerView.visibility=View.GONE
        }
        else
        {
            loadAppBanner(this@AllGifA)
            binding.tabNoData.visibility=View.GONE
            binding.recyclerView.visibility=View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
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

}