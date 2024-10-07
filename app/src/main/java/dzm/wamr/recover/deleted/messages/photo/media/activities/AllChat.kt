package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.mindorks.Screenshot
import com.mindorks.properties.Quality
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ChatAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityAllChatBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Model
import dzm.wamr.recover.deleted.messages.photo.media.sqlDb.DatabaseHelper
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.MyApp.Companion.showEvenAd
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class AllChat : AppCompatLocaleActivity() {
    var name: String? = null
    var date: String? = null
    var msg: String? = null
    var time: String? = null
    var type: String? = null
    var grpMbr: String? = null
    var databaseHelper: DatabaseHelper? = null
    var adapter: ChatAdapter? = null
    val list: MutableList<Model> = ArrayList()
    var data: Cursor? = null
    var isClicked: Boolean = false
    lateinit var binding:ActivityAllChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_all_chat)
        databaseHelper = DatabaseHelper(binding.root.context)
        if(intent.hasExtra("name"))
        {
            name="${intent.getStringExtra("name")}"
            binding.tvName.text = name
            binding.tvPhoneNo.text=name
            if (Utils.isNumeric("${name}"))
            {
                binding.tvPhoneNo.visibility=View.VISIBLE
                binding.tvName.visibility=View.GONE
            }
            else
            {
                binding.tvPhoneNo.visibility=View.GONE
                binding.tvName.visibility=View.VISIBLE
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(MyDataUpdateReceiver(), IntentFilter(ACTION_NEW_NOTIFICATION))
        loadMessages(name)
        val window = window

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (AdConst.IS_AD_SHOW) {
                    if (showEvenAd == 1) {
                        showEvenAd = 0
                        isClicked = true
                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AllChat,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AllChat,Const.AD_NEXT_REQUEST_DELAY)
                                    if (isClicked) {
                                        finish()
                                    } else {
                                        Utils.showToast(binding.root.context,
                                        binding.root.context.getString(R.string.saved_in_downloads))
                                    }

                                }
                            },true)
                    } else {
                        showEvenAd = 1
                        finish()
                    }
                } else {
                    finish()
                }
            }

        }

        onBackPressedDispatcher.addCallback(this, callback)
        binding.backArrowIv.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.screenShot.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            binding.tvTime.text = binding.root.context.getText(R.string.app_name)

            if (AdConst.IS_AD_SHOW) {
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AllChat,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AllChat,
                                Const.AD_NEXT_REQUEST_DELAY)

                            saveScreenShotOfChat(binding.screen)
                        }
                    },true)

            } else {
                saveScreenShotOfChat(binding.screen)
            }
        }))

        initAdapter()
    }

    var avoidMultipleSelection=true
    private fun initAdapter() {
        binding.chatRecycler.setHasFixedSize(true)
        // use a linear layout manager
        val layoutManager=LinearLayoutManager(binding.root.context)
        layoutManager.orientation=LinearLayoutManager.VERTICAL
        binding.chatRecycler.layoutManager = layoutManager
        adapter = ChatAdapter(list,object:AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                val itemSelected:Model=list.get(position)
                Log.d(Const.tag,"ChatItem: "+itemSelected.getMsg())
                if (avoidMultipleSelection)
                {
                    avoidMultipleSelection=false
                    if(itemSelected.getMsg().contains("\uD83D\uDCF7 Photo",true))
                    {
                        moveToMediaWithAd(0)
                    } else if(itemSelected.getMsg().contains("\uD83D\uDC7E GIF",true))
                    {
                        moveToMediaWithAd(1)
                    } else if(itemSelected.getMsg().contains("\uD83C\uDFA5 Video",true))
                    {
                        moveToMediaWithAd(2)
                    } else if(itemSelected.getMsg().contains("\uD83C\uDFB5 Voice message",true))
                    {
                        moveToMediaWithAd(3)
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    avoidMultipleSelection=true
                },300)
            }
        })
        binding.chatRecycler.adapter = adapter
    }

    private fun moveToMediaWithAd(tabPositon:Int) {
        if (AdConst.IS_AD_SHOW) {
            InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AllChat,
                object: AdCallback {
                    override fun onAdComplete(isCompleted: Boolean) {
                        InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AllChat,Const.AD_NEXT_REQUEST_DELAY)
                        moveToMediaAfterAd(tabPositon)
                    }
                },true)

        } else {
            moveToMediaAfterAd(tabPositon)
        }
    }

    private fun moveToMediaAfterAd(tabPositon:Int) {
        val callingIntent=Intent(binding.root.context, ShowMeidaA::class.java)
        callingIntent.putExtra("tabPositon",tabPositon)
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    private fun loadMessages(name: String?) {
        list.clear()
        databaseHelper?.let { database->
            data = database.getSpecificData(name)
            while (data?.moveToNext()!!) {
                date = data?.getString(3)
                msg = data?.getString(2)
                time = data?.getString(4)
                type = data?.getString(6)
                grpMbr = data?.getString(7)
                if (msg?.contains("new messages")!!) {
                    Log.i("message", "new message")
                } else {
                    list.add(Model(msg, time, date, type, grpMbr, false))
                }
            }
            data?.close()
        }

//        for (model in list) {
//            val modelToBeDeleted = list[list.indexOf(model) - 1]
//            val isDeleted = model.msg.contains("This message was deleted")
//            if (isDeleted) {
//                modelToBeDeleted.setIdDeleted(true)
//                list[list.indexOf(model) - 1] = modelToBeDeleted
//                list.removeAt(list.indexOf(model))
//            }
//        }


        adapter?.notifyDataSetChanged()
        binding.chatRecycler.scrollToPosition(list.size - 1)
        if (list.size>0)
        {
            loadAppNative(this@AllChat)
        }

    }

    inner class MyDataUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get the data from the intent
            loadMessages(name)
        }
    }

    private var nativeManger: NativeAdManager?= null
    private fun loadAppNative(activity: Activity) {
        if (AdConst.IS_AD_SHOW)
        {
            if (nativeManger==null && Common.isNetworkAvailable(activity))
            {
                nativeManger= NativeAdManager(activity)
                val nativeId=binding.root.context.getString(R.string.native_ads)
                nativeManger?.showAndLoadNativeAd(nativeId,binding.tabNative,binding.nativeTemplate,object: AdListener(){
                    override fun onAdFailedToLoad(adError : LoadAdError) {
                        Log.d(NativeAdManager.TAG_NATIVE,"Native Failure: $adError")
                        binding.tabNative.visibility= View.GONE
                        nativeManger=null
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d(NativeAdManager.TAG_NATIVE,"Native: ")
                        binding.nativeTemplate.visibility= View.VISIBLE
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

    private fun saveScreenShotOfChat(view: View) {
        val bitmap = Screenshot.with(this@AllChat)
            .setView(view)
            .setQuality(Quality.HIGH)
            .getScreenshot()
        val imgName = "${System.currentTimeMillis()}.jpg"

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            saveAEImage(bitmap, imgName)
        } else {
            ImageSaver(this@AllChat, false)
                .setFileName(imgName)
                .setDirectoryName("Screenshots")
                .save(bitmap)
        }
        binding.tvTime.text=""
    }

    private fun saveAEImage(bitmap: Bitmap, imageName: String) {
        var outputStream: OutputStream? = null
        try {
            val resolver: ContentResolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Screenshots")
            }
            val imagePhoto: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = imagePhoto?.let { resolver.openOutputStream(it) }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream?.close()
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.saved_in_downloads))
        } catch (e: Exception) {
            Log.d(Const.tag, "Exception : $e")
        } finally {
            outputStream?.close()
        }
    }




    companion object {
        var ACTION_NEW_NOTIFICATION = "ACTION_NEW_NOTIFICATION"
    }
}