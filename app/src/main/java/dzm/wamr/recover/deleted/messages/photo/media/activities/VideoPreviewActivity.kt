package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityVideoPreviewBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class VideoPreviewActivity : AppCompatLocaleActivity() {
    var videoPath: String? = null
    lateinit var binding:ActivityVideoPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_video_preview)
        videoPath = intent.getStringExtra("videoPath")
        binding.displayVV.setVideoPath(videoPath)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.displayVV)
        binding.displayVV.setMediaController(mediaController)
        binding.displayVV.start()


        binding.backIV.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
    }

    override fun onPause() {
        super.onPause()
        if (binding.displayVV.isPlaying)
        {
            binding.displayVV.pause()
        }
    }

    override fun onBackPressed() {
        binding.displayVV.stopPlayback()
        if (AdConst.IS_AD_SHOW) {
            if (MyApp.showEvenAd == 1) {
                MyApp.showEvenAd = 0

                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@VideoPreviewActivity,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@VideoPreviewActivity,
                                Const.AD_NEXT_REQUEST_DELAY)

                            finishWithResult()
                        }
                    },true)
            } else {
                MyApp.showEvenAd = 1
                finishWithResult()
            }
        } else {
            finishWithResult()
        }
    }

    private fun finishWithResult() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }


}