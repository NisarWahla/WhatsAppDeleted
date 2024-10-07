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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.EmojiAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityEmojiBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class EmojiA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityEmojiBinding
    var dataList:ArrayList<String> = ArrayList()
    var adapter: EmojiAdapter? =null
    var selectedCategoryPosition:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_emoji)

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
            binding.tvTitle.text=binding.root.context.getString(R.string.smiles).uppercase()
        } else if (position==1)
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.people).uppercase()
        } else if (position==2)
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.nature).uppercase()
        } else
        {
            binding.tvTitle.text=binding.root.context.getString(R.string.places).uppercase()
        }
    }

    var avoidMultipleSelection=true
    private fun setupAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context).apply { orientation=LinearLayoutManager.VERTICAL }
        adapter= EmojiAdapter(dataList, object :AdapterClicklistener{
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
        binding.recyclerView.adapter=adapter


        Handler(Looper.getMainLooper()).postDelayed({
            getEmojiList()
        },200)
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



    private fun copyOnClipboard(emoji: String) {
        try {
            val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(binding.root.context.getString(R.string.copied), emoji)
            clipboard.setPrimaryClip(clip)
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.emoji_copy_in_clipboard))

        } catch (e: Exception) {
            Log.d(Const.tag, "Exception : $e")
        }
    }

    private fun getEmojiList() {
        binding.progressBar.visibility=View.VISIBLE
        dataList.clear()
        var emojiArray:Array<String>?=null
        if (selectedCategoryPosition==0)
        {
         emojiArray = binding.root.context.resources.getStringArray(R.array.smiles_emojies)
        } else if (selectedCategoryPosition==1)
        {
            emojiArray = binding.root.context.resources.getStringArray(R.array.people_emojies)
        } else if (selectedCategoryPosition==2)
        {
            emojiArray = binding.root.context.resources.getStringArray(R.array.nature_emojies)
        } else if (selectedCategoryPosition==3)
        {
            emojiArray = binding.root.context.resources.getStringArray(R.array.places_emojies)
        }
        emojiArray?.let {
            for (emoji in it) {
                try {
                    dataList.add(Utils.convertEmoji(emoji))
                } catch (e: Exception) {
                    // Handle the exception as needed
                }
            }
        }
        binding.progressBar.visibility=View.GONE
        if(!(dataList.isEmpty()))
        {
            loadAppBanner(this@EmojiA)
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