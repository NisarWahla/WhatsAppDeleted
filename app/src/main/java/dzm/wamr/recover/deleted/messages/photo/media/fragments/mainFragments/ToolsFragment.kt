package dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.*
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentToolsBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*


enum class FirstTourTools{WEB_ACT,ASCII_ACT,DIRECT_CHAT_ACT,TEXT_REPEATER_ACT,EMOJI,GIF}
class ToolsFragment : Fragment() {

    lateinit var binding: FragmentToolsBinding


    companion object {
        fun newInstance(): ToolsFragment {
            val fragment = ToolsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tools,container,false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.tabWhatsWeb.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "WhatsWeb")
            MoveToNextActivityWithFirstTour(FirstTourTools.WEB_ACT)

        }))
        binding.tabAsciiFaces.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "AsciiFaces")
            MoveToNextActivityWithFirstTour(FirstTourTools.ASCII_ACT)
        }))
        binding.tabDirectMsg.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "DirectChat")
            MoveToNextActivityWithFirstTour(FirstTourTools.DIRECT_CHAT_ACT)
        }))
        binding.tabRepeatText.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "TextRepeater")
            MoveToNextActivityWithFirstTour(FirstTourTools.TEXT_REPEATER_ACT)
        }))
        binding.tabEmoji.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "Emoji")
            MoveToNextActivityWithFirstTour(FirstTourTools.EMOJI)
        }))
        binding.tabGif.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            Utils.addEventToFirebase(MyApp.buttonClick, "Gif")
            MoveToNextActivityWithFirstTour(FirstTourTools.GIF)
        }))
    }

    private fun MoveToNextActivityWithFirstTour(type: FirstTourTools) {
        if (AdConst.IS_AD_SHOW) {
            activity?.let {
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(it,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,Const.AD_NEXT_REQUEST_DELAY)
                            AdConst.firstTourAd=false
                            MoveToNextActivityAfterFirstTour(type)

                        }
                    },true)
            }

        } else {
            MoveToNextActivityAfterFirstTour(type)
        }
    }

    private fun MoveToNextActivityAfterFirstTour(type: FirstTourTools) {
        if (type==FirstTourTools.WEB_ACT)
        {
            callActivity(WebActivity::class.java)
        }else if (type==FirstTourTools.ASCII_ACT)
        {
            callActivity(AsciiCategoryA::class.java)
        }else if (type==FirstTourTools.DIRECT_CHAT_ACT)
        {
            callActivity(DirectChat::class.java)
        }else if (type==FirstTourTools.TEXT_REPEATER_ACT)
        {
            callActivity(TextRepeater::class.java)
        }else if (type==FirstTourTools.EMOJI)
        {
            callActivity(EmojiCategoryA::class.java)
        }else if (type==FirstTourTools.GIF)
        {
            callActivity(AllGifA::class.java)
        }
    }

    private fun callActivity(javaClassName: Class<*>) {
        startActivity(Intent(binding.root.context, javaClassName))
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun initControl() {

    }


    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                activity?.let {
                    loadAppNative(it)
                }
            },200)
        }
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

    fun checkIsNeedToRemoveAd(){
        activity?.let {
            loadAppNative(it)
        }
    }

}