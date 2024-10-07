package dzm.wamr.recover.deleted.messages.photo.media.admanager


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.smartnsoft.backgrounddetector.BackgroundDetectorCallback
import com.smartnsoft.backgrounddetector.BackgroundDetectorHandler
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.SplashA
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.util.Common
import dzm.wamr.recover.deleted.messages.photo.media.util.DialogUtil
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import java.util.*

class MyAppOpenAd(
    private val application: Application
){

    private var backgroundDetectorHandler: BackgroundDetectorHandler? = null
    private var adId: String
    private var activity: Activity? = null
    private var appOpenAd: AppOpenAd? = null

    companion object {
        private const val TAG = "MyAppOpenAd"
        var isInterstitialClose=true
        var isDialogClose=true
        var adLoadingInProgress=false
        var isAppInBackground=false
    }



    val activityLifecycleCallback=object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
            activity = p0
            backgroundDetectorHandler?.onActivityResumed(activity)
            if (!(Common.isNetworkAvailable(application.applicationContext)))
            {
                return
            }
            if (activity is SplashA)
            {
                isAppInBackground=false
                return
            }
            if (isAppInBackground && isInterstitialClose && isDialogClose)
            {
                Log.d(TAG, "onActivityResumed")
                isAppInBackground=false
                if (appOpenAd == null && AdConst.IS_AD_SHOW && !(adLoadingInProgress))
                {
                    DialogUtil.showAdLoadingDialog(p0)
                    fetchAndShowAd()
                }
            }
        }



        override fun onActivityPaused(p0: Activity) {
            backgroundDetectorHandler?.onActivityPaused(activity)
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }
    }


    init {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)
        backgroundDetectorHandler = BackgroundDetectorHandler(BackgroundDetectorCallback(BackgroundDetectorHandler.ON_ACTIVITY_RESUMED,
            object : BackgroundDetectorHandler.OnVisibilityChangedListener{
                override fun onAppGoesToBackground(context: Context?) {
                    Log.d(TAG, "onAppGoesToBackground")
                    isAppInBackground=true
                }

                override fun onAppGoesToForeground(context: Context?) {
                    Log.d(TAG, "onAppGoesToForeground")

                }
            }))
        adId = application.getString(R.string.admob_open_ads_id)
    }

    fun getLifecycleCallback():Application.ActivityLifecycleCallbacks{
        return activityLifecycleCallback
    }

    private fun fetchAndShowAd() {
        Log.e(TAG, "Interstial preLoadAllScreenInterstital is:${InterstitialManager.preLoadAllScreenInterstital?.IsRequestInProgress()}")

        if (appOpenAd == null && AdConst.IS_AD_SHOW) {
            val adRequest = AdRequest.Builder().build()
            val callback = object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d(TAG, "onAdFailedToLoad: " + p0.message)
                    adLoadingInProgress=false
                    DialogUtil.closeAdLoadingDialog()
                    appOpenAd = null
                }

                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    adLoadingInProgress=false
                    DialogUtil.closeAdLoadingDialog()
                    appOpenAd = p0
                    loadTime = Date().time
                    Log.d(TAG, "onAdLoaded: ")
                    justShowAd()
                }
            }
            adLoadingInProgress=true
            AppOpenAd.load(application, adId, adRequest, callback)
            Log.d(TAG, "OpenAppAd: RequestCall: ")
        }
        else
        {
            justShowAd()
        }
    }

    private fun justShowAd() {
        if (appOpenAd != null) {
            val callback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d(TAG, "onAdDismissedFullScreenContent: ")
                    appOpenAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    Log.d(TAG, "onAdFailedToShowFullScreenContent: ${p0.message}")
                    appOpenAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d(TAG, "onAdShowedFullScreenContent: ")
                }
            }

            appOpenAd?.fullScreenContentCallback = callback
            activity?.let {
                if (isAdAvailable())
                {
                    appOpenAd?.show(it)
                }
            }
            Log.d(TAG, "OpenAppAd: ShowRequestCall: ")
        }
        else
        {
            Log.d(TAG, "OpenAppAd: null instence: ")
        }
    }

    private var loadTime: Long = 0
    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    fun isAdAvailable(): Boolean {
        // Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
        // https://support.google.com/admob/answer/9341964?hl=en
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

}