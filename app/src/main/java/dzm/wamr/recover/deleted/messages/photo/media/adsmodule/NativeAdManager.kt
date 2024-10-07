package dzm.wamr.recover.deleted.messages.photo.media.admanager

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.util.Common

class NativeAdManager(val activity: Activity) {

    companion object{
        const val  TAG_NATIVE = "NativeAdTest_"
    }

    var mainNativeAd:NativeAd? =null

    fun showAndLoadNativeAd(nativeId:String,container: ViewGroup, template: TemplateView, listener: AdListener)
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
            container.visibility= View.VISIBLE
           val adLoader = AdLoader.Builder(
                activity, nativeId)
                .forNativeAd { nativeAd ->
                    val styles = NativeTemplateStyle.Builder().build()
                    template.setStyles(styles)
                    mainNativeAd=nativeAd
                    mainNativeAd?.let {
                        template.setNativeAd(it)
                    }
                }.withAdListener(listener).build()
            adLoader.loadAd(AdRequest.Builder().build())

        } catch (e: Exception) {
           Log.d(TAG_NATIVE,"Native Loading Exception: ${e}")
        }
    }

    fun destroyNativeAd() {
        mainNativeAd?.destroy()
    }

}