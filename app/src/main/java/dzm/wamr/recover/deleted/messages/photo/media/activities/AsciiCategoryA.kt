package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityAsciiCategoryBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class AsciiCategoryA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityAsciiCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_ascii_category)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
        binding.tabHappy.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            moveToCategory(0)
        }))
        binding.tabAngry.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            moveToCategory(1)
        }))
        binding.tabOther.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            moveToCategory(2)
        }))
    }

    private fun moveToCategory(position: Int) {
        if (AdConst.IS_AD_SHOW) {
            InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AsciiCategoryA,
                object: AdCallback {
                    override fun onAdComplete(isCompleted: Boolean) {
                        InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AsciiCategoryA,
                            Const.AD_NEXT_REQUEST_DELAY)
                        MoveToNextStepAfterAd(position)
                    }
                },true)

        } else {
            MoveToNextStepAfterAd(position)
        }
    }

    val resultCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
            result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {

            }
        }
    }
    }
    private fun MoveToNextStepAfterAd(position: Int) {
        val intent = Intent(binding.root.context, AsciiFacesActivity::class.java)
        intent.putExtra("position",position)
        resultCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }



    private fun initControl() {
        loadAppNative(this@AsciiCategoryA)
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
    override fun onBackPressed() {
        if (AdConst.IS_AD_SHOW) {
            if (MyApp.showEvenAd == 1) {
                MyApp.showEvenAd = 0
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@AsciiCategoryA,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@AsciiCategoryA,
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