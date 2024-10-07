package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.AsciAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityAsciiFacesBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.AsciFaces
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class AsciiFacesActivity : AppCompatLocaleActivity() {

    lateinit var binding: ActivityAsciiFacesBinding
    var selectedCategoryPosition:Int=0
    var asciAdapter: AsciAdapter? =null
    var dataList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_ascii_faces)

        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
    }

    private fun initControl() {
        selectedCategoryPosition=intent.getIntExtra("position",0)
        setupTitle(selectedCategoryPosition)
        setupAdapter()
    }

    private fun setupTitle(position: Int) {
        if (position==0)
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.happy).uppercase()
        } else if (position==1)
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.angry).uppercase()
        } else
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.other).uppercase()
        }
    }

    private fun populateDatalist() {
        try {
            val asciFaces:ArrayList<AsciFaces> = Gson().fromJson(loadJSONFromRaw(R.raw.asci_faces, binding.root.context), object : TypeToken<ArrayList<AsciFaces>>() {}.type)

            dataList.clear()
            dataList.addAll(asciFaces.get(selectedCategoryPosition).data)

            if (!(dataList.isEmpty())){
                loadAppBanner(this@AsciiFacesActivity)
            }

        }catch (e:java.lang.Exception){
            Log.d(Const.tag,"Exception: $e")
        }
    }

    var avoidMultipleSelection=true
    private fun setupAdapter() {
        populateDatalist()
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context).apply { orientation=LinearLayoutManager.VERTICAL }
        asciAdapter = AsciAdapter(dataList, object : AdapterClicklistener {
            override fun onItemClick(view: View, position: Int) {
                val item=dataList.get(position)
                if (avoidMultipleSelection)
                {
                    avoidMultipleSelection=false
                    if (view.id==R.id.ivShare)
                    {
                        shareAsciiByIntent(item)
                    }
                    if (view.id==R.id.ivCopy)
                    {
                       copyOnClipboard(item)
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    avoidMultipleSelection=true
                },300)
            }
        })
        binding.recyclerView.adapter = asciAdapter
    }

    private fun copyOnClipboard(emoji: String) {
        try {
            val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(binding.root.context.getString(R.string.copied), emoji)
            clipboard.setPrimaryClip(clip)
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.ascii_face_copy_in_clipboard))

        } catch (e: Exception) {
            Log.d(Const.tag, "Exception : $e")
        }
    }


    fun loadJSONFromRaw(resourceId: Int, context: Context): String {
        return context.resources.openRawResource(resourceId).bufferedReader()
            .use { it.readText() }
    }
    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }
    private fun shareAsciiByIntent(item: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, item)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val chooserIntent = Intent.createChooser(shareIntent, binding.root.context.getString(R.string.share_via))
        if (shareIntent.resolveActivity(binding.root.context.packageManager) != null) {
            MyAppOpenAd.isDialogClose=false
            resultShareCallback.launch(chooserIntent)
        }
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


    override fun onBackPressed() {
        if (AdConst.IS_AD_SHOW) {
            if (MyApp.showEvenAd == 1) {
                MyApp.showEvenAd = 0
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AsciiFacesActivity,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AsciiFacesActivity,
                                Const.AD_NEXT_REQUEST_DELAY)
                            moveToBack()
                        }
                    },true)
            } else {
                MyApp.showEvenAd = 1
                moveToBack()
            }
        } else {
            moveToBack()
        }
    }

    private fun moveToBack() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }
}