package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityDirectChatBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class DirectChat : AppCompatLocaleActivity() {
    private lateinit var binding: ActivityDirectChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_direct_chat)
        loadAppNative(this@DirectChat)
        binding.tabSend.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            if (binding.phone.text.isEmpty()) {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.enter_phone))
                return@OnClickListener
            }
            sendMessage()
        }))

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (AdConst.IS_AD_SHOW) {
                    if (MyApp.showEvenAd == 1) {
                        MyApp.showEvenAd = 0
                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@DirectChat,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@DirectChat,
                                        Const.AD_NEXT_REQUEST_DELAY)

                                    finish()
                                }
                            },true)
                    } else {
                        MyApp.showEvenAd = 1
                        finish()
                    }
                } else {
                    finish()
                }
            }

        }
        onBackPressedDispatcher.addCallback(this, callback)
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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
    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }
    private fun sendMessage() {
        val i = Intent(Intent.ACTION_VIEW)
        val code = binding.countyCodePicker.selectedCountryCode.toString()
        val phone = code + binding.phone.text.toString().trim()
        val msg = binding.msg.text.toString().trim()
        try {
            val url = "https://api.whatsapp.com/send?phone=$phone&text=$msg"
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (i.resolveActivity(packageManager) != null) {
                MyAppOpenAd.isDialogClose=false
                resultShareCallback.launch(Intent.createChooser(i, binding.root.context.getString(R.string.not_found)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}