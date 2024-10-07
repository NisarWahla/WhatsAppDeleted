package cdzm.wamr.recover.deleted.messages.photo.media.admanager


import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.util.Common
import dzm.wamr.recover.deleted.messages.photo.media.util.Const
import dzm.wamr.recover.deleted.messages.photo.media.util.DialogUtil


class InterstitialManager(val context: Context) {

    private val TAG_INTERSTITIAL = "InterstitialAdTest_"

    var mInterstitialAd: InterstitialAd? = null
    private var isAddLoaded=false
    private var isRequestInProgress=false
    var callback: AdCallback?= null
//    request and show progress
    private var isRASRequestInProgress=false

    companion object{
        var preLoadAllScreenInterstital: InterstitialManager?=null
    }

    fun loadInterstial(InterstialId:String) {
        if (!(Common.isNetworkAvailable(context)))
        {
            return
        }
        if (isRequestInProgress)
        {
            return
        }
        if (isAddLoaded)
        {
            return
        }
        val adRequest = AdRequest.Builder().build()
        isRequestInProgress=true
        InterstitialAd.load(context,
            InterstialId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadingError: LoadAdError) {
                    super.onAdFailedToLoad(loadingError)
                    Log.e(TAG_INTERSTITIAL, "Ad is Failed To Load Code: ${loadingError.code}")
                    callback?.onAdComplete(false)
                    callback=null
                    mInterstitialAd = null
                    isAddLoaded=false
                    isRequestInProgress=false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    Log.e(TAG_INTERSTITIAL, "Ad is Loaded")
                    mInterstitialAd = interstitialAd
                    isAddLoaded=true
                    isRequestInProgress=false
                    registerListener()
                }
            })
    }

    fun showInterstitial(activity:Activity,callback:AdCallback,isAddShow:Boolean=false) {
        Log.d(TAG_INTERSTITIAL,"MyCurrentAdLimit showInterstitial: ${AdConst.MyCurrentAdLimit}")
        if(AdConst.MyCurrentAdLimit==-1 || AdConst.MyCurrentAdLimit>AdConst.REQUEST_AND_SHOW_AD_LIMIT)
        {
            Log.e(TAG_INTERSTITIAL, "show method call isRequestInProgress:$isRequestInProgress  isAddLoaded: $isAddLoaded")
            Log.e(TAG_INTERSTITIAL, "openAd adLoadingInProgress is:${MyAppOpenAd.adLoadingInProgress}")
            Log.e(TAG_INTERSTITIAL, "openAd isAppInBackground is:${MyAppOpenAd.isAppInBackground}")
            this.callback=callback
            if (Common.isNetworkAvailable(activity))
            {
                if (MyAppOpenAd.adLoadingInProgress)
                {
                    this.callback?.onAdComplete(false)
                    this.callback=null
                }
                else
                {
                    if (isAddLoaded)
                    {
                        showAdWithProgress(isAddShow,activity)
                    }
                    else
                    {
                        this.callback?.onAdComplete(false)
                        this.callback=null
                    }
                }
            }
            else
            {
                this.callback?.onAdComplete(false)
                this.callback=null
            }
        }
        else
        {
            if (Common.isNetworkAvailable(activity))
            {
                if (mInterstitialAd!=null && IsAdLoaded())
                {
                    showAdWithProgress(isAddShow,activity)
                }
                else
                {
                    requestAndLoadScenarioApply(activity,callback)
                }
            }
            else
            {
                callback.onAdComplete(false)
            }
        }


    }

    private fun showAdWithProgress(isAddShow:Boolean,activity:Activity) {
        if (isAddShow)
        {
            DialogUtil.showAdLoadingDialog(activity)
            Handler(Looper.getMainLooper()).postDelayed({
                activity.runOnUiThread{
                    DialogUtil.closeAdLoadingDialog()
                }
                mInterstitialAd?.show(activity)
            },500)
        }
        else
        {
            mInterstitialAd?.show(activity)
        }
    }

    private fun requestAndLoadScenarioApply(activity: Activity,callback:AdCallback,) {
        if (isRASRequestInProgress)
        {
            Log.e(TAG_INTERSTITIAL, "RAS Ad is runing")
            callback.onAdComplete(false)
            return
        }
        val adRequest = AdRequest.Builder().build()
        DialogUtil.showAdLoadingDialog(activity)
        isRASRequestInProgress=true
        InterstitialAd.load(context,
            context.getString(R.string.admob_interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadingError: LoadAdError) {
                    super.onAdFailedToLoad(loadingError)
                    Log.e(TAG_INTERSTITIAL, "RAS Ad is Failed To Load Code: ${loadingError.code}")
                    DialogUtil.closeAdLoadingDialog()
                    isRASRequestInProgress=false
                    callback.onAdComplete(false)
                    AdConst.MyCurrentAdLimit=AdConst.MyCurrentAdLimit+1
                    newAdLogicRequest(activity)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    Log.e(TAG_INTERSTITIAL, "RAS Ad is Loaded")
                    interstitialAd.setFullScreenContentCallback(object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG_INTERSTITIAL, "RAS Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            MyAppOpenAd.isInterstitialClose=true
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG_INTERSTITIAL, "RAS Ad dismissed fullscreen content.")
                            isRASRequestInProgress=false
                            callback.onAdComplete(true)
                            AdConst.MyCurrentAdLimit=AdConst.MyCurrentAdLimit+1
                            newAdLogicRequest(activity)

                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            Log.e(TAG_INTERSTITIAL, "RAS Ad failed to show fullscreen content. ${adError.code}")
                            isRASRequestInProgress=false
                            callback.onAdComplete(false)
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG_INTERSTITIAL, "RAS Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            MyAppOpenAd.isInterstitialClose=false
                            // Called when ad is shown.
                            Log.d(TAG_INTERSTITIAL, "RAS Ad showed fullscreen content.")
                        }
                    })
                   activity.runOnUiThread {
                       DialogUtil.closeAdLoadingDialog()
                   }
                    interstitialAd.show(activity)
                }
            })
    }

    private fun newAdLogicRequest(activity: Activity) {
        Log.d(TAG_INTERSTITIAL,"RAS Ad is newAdLogicRequest: ${AdConst.MyCurrentAdLimit}")
        if(AdConst.MyCurrentAdLimit==-1 || AdConst.MyCurrentAdLimit>AdConst.REQUEST_AND_SHOW_AD_LIMIT)
        {
            preLoadAllScreenInterstital?.
            requestInterstitialAd(activity,Const.AD_NEXT_REQUEST_DELAY)
        }

    }

    private fun registerListener(){
        mInterstitialAd?.setFullScreenContentCallback(object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG_INTERSTITIAL, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                MyAppOpenAd.isInterstitialClose=true
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG_INTERSTITIAL, "Ad dismissed fullscreen content.")
                AdConst.MyCurrentAdLimit=AdConst.MyCurrentAdLimit+1
                callback?.onAdComplete(true)
                callback=null
                mInterstitialAd = null
                isAddLoaded=false
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                Log.e(TAG_INTERSTITIAL, "Ad failed to show fullscreen content. ${adError.code}")
                callback?.onAdComplete(false)
                callback=null
                mInterstitialAd = null
                isAddLoaded=false
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG_INTERSTITIAL, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                MyAppOpenAd.isInterstitialClose=false
                // Called when ad is shown.
                Log.d(TAG_INTERSTITIAL, "Ad showed fullscreen content.")
            }
        })
    }

    fun requestInterstitialAd(context: Context, interval:Long=0) {
        Log.d(TAG_INTERSTITIAL,"MyCurrentAdLimit requestInterstitialAd: ${AdConst.MyCurrentAdLimit}")
        if(AdConst.MyCurrentAdLimit==-1 || AdConst.MyCurrentAdLimit>AdConst.REQUEST_AND_SHOW_AD_LIMIT)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                if (AdConst.IS_AD_SHOW)
                {
                    Log.d(TAG_INTERSTITIAL,"Ad Request is launch:")
                    preLoadAllScreenInterstital?.loadInterstial(context.getString(R.string.admob_interstitial_id))
                }
            },interval)
        }
    }



    fun IsAdLoaded():Boolean {
        return isAddLoaded
    }


    fun IsRequestInProgress():Boolean {
        return isRequestInProgress
    }



}