package dzm.wamr.recover.deleted.messages.photo.media.admanager

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.util.Common

class BannerManager(val activity: Activity) {

    companion object{
        const val  TAG_BANNER = "BannerAdTest_"
    }

    fun showAndLoadCollapsibleBannerAd(bannerId:String,container:ViewGroup,listener: AdListener,collapsibleType:String="bottom")
    {
        if (!(Common.isNetworkAvailable(activity)))
        {
            return
        }
        if(!(AdConst.IS_AD_SHOW))
        {
            return
        }

        try {
            val adView = AdView(activity)

            adView.adUnitId = bannerId

            val adSize = adSize(container)
            adView.setAdSize(adSize)
            container.visibility= View.VISIBLE
            container.addView(adView)


            val extras = Bundle()
            extras.putString("collapsible", "${collapsibleType}")
            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
            adView.loadAd(adRequest)
            Log.d(TAG_BANNER,"Banner Requested")
            adView.adListener = listener
        } catch (e: Exception) {
            Log.d(TAG_BANNER,"Banner Loading Exception: ${e}")
        }
    }

    fun showAndLoadBannerAd(bannerId:String,container:ViewGroup,listener: AdListener)
    {
        if (!(Common.isNetworkAvailable(activity)))
        {
            return
        }
        if(!(AdConst.IS_AD_SHOW))
        {
            return
        }

        try {
            val adView = AdView(activity)

            adView.adUnitId = bannerId

            val adSize = adSize(container)
            adView.setAdSize(adSize)
            container.visibility= View.VISIBLE
            container.addView(adView)
            val request = AdRequest.Builder().build()
            adView.loadAd(request)
            Log.d(TAG_BANNER,"Banner Requested")
            adView.adListener = listener
        } catch (e: Exception) {
           Log.d(TAG_BANNER,"Banner Loading Exception: ${e}")
        }
    }

    fun adSize(container:ViewGroup): AdSize {
        try {
            // Determine the screen width (less decorations) to use for the ad width.
            val display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = container.width.toFloat()
            // If the ad hasn't been laid out, default to the full screen width.
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        } catch (e: Exception) {
            Log.d(TAG_BANNER,"Banner Size Exception: ${e}")
            return AdSize.BANNER
        }
    }

}