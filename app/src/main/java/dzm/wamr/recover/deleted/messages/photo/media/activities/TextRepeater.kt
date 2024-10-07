package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.*
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityTextRepeaterBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.FragmentCallBack
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.ResultCallBack
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets

class TextRepeater : AppCompatLocaleActivity() {
    lateinit var binding: ActivityTextRepeaterBinding
    private var noOfRepeat: Int = 0
    private var isNewLine: Boolean = false
    private var content = ""
    var isCopyClicked: Boolean = false
    var isAllowApply=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_text_repeater)
        loadAppBanner(this@TextRepeater)


        binding.noOfRepeatText.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 1000) { text ->
                checkMaxLimit(text)
            }
        )

        binding.tabShare.setOnClickListener {
            shareContent()
        }
       /* binding.root.viewTreeObserver.addOnWindowFocusChangeListener {
            if (AdConst.IS_AD_SHOW) {
                if (it && !app.getShowingAd()) {
                    app.showAppOpenAd()
                }
            }
        }*/
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (AdConst.IS_AD_SHOW) {
                    if (MyApp.showEvenAd == 1) {
                        MyApp.showEvenAd = 0
                        isCopyClicked = false

                        InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@TextRepeater,
                            object: AdCallback {
                                override fun onAdComplete(isCompleted: Boolean) {
                                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@TextRepeater,
                                        Const.AD_NEXT_REQUEST_DELAY)

                                   performStepAfterAd()
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

        binding.newLineSwitchCompat.setOnCheckedChangeListener { buttonView, isChecked ->

        }
        binding.tabClear.setOnClickListener {
            if (binding.result.length() != 0) {
                binding.result.setText("")
            } else {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.nothing_to_delete))
            }
        }

        binding.tabCopy.setOnClickListener {
            if (binding.result.text.isEmpty()) {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.result_field_empty))
                return@setOnClickListener
            }
            if (AdConst.IS_AD_SHOW) {
                isCopyClicked = true
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@TextRepeater,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@TextRepeater,
                                Const.AD_NEXT_REQUEST_DELAY)
                            performStepAfterAd()
                        }
                    },true)
            } else {
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                    ClipData.newPlainText(
                        binding.text.text.toString(), binding.result.text.toString()
                    )
                )
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.copied_to_clipboard))
            }
        }
        binding.tabRepeat.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            if (isAllowApply)
            {
                performRepeatedText()
            }
        }))
    }





    private fun performRepeatedText() {
        hideKeyboard(binding.tabRepeat)
        if (binding.text.text.isEmpty()) {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_enter_repeat_text))
            return
        }

        try {
            noOfRepeat = Integer.valueOf(binding.noOfRepeatText.text.toString())
        } catch (ex: java.lang.Exception) {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_enter_no_repeat_text))
            return
        }
        if (noOfRepeat>5000)
        {
            Utils.showToast(binding.root.context, binding.root.context.getString(R.string.repeat_limit))
            return
        }


        isAllowApply=false
        isNewLine = binding.newLineSwitchCompat.isChecked
        content = binding.text.text.toString()
        binding.progressBar.visibility=View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            // Call repeatStringAsync with a callback
            repeatStringAsync(content, noOfRepeat, isNewLine) { result ->
               runOnUiThread {
                   binding.result.setText("${result}")
                   isAllowApply=true
                   binding.progressBar.visibility=View.GONE
               }
            }
        }
    }


    suspend fun repeatStringAsync(
        input: String,
        repeatTimes: Int,
        startOnNewLine: Boolean = false,
        callback: (String) -> Unit
    ) = withContext(Dispatchers.Default) {
        val repeatedString = StringBuilder()

        if (startOnNewLine) {
            repeatedString.appendLine(input) // Append input line at the beginning if startOnNewLine is true
        } else {
            repeatedString.append(input) // Append input string at the beginning if startOnNewLine is false
        }

        repeat(repeatTimes - 1) { // Repeat one less time, as we've already appended the input string once
            if (startOnNewLine) {
                repeatedString.appendLine(input) // Append input line if startOnNewLine is true
            } else {
                repeatedString.append(" $input") // Append input string with space if startOnNewLine is false
            }
        }

        val result = repeatedString.toString()
        callback(result)
    }



    private fun checkMaxLimit(text: String) {
        try {
            if (text.isEmpty())
            {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_enter_no_repeat_text))
            } else
            {
                val number=Integer.parseInt(text)
                if (number>5000)
                {
                    Utils.showToast(binding.root.context, binding.root.context.getString(R.string.repeat_limit))
                }

            }
        } catch (e: java.lang.Exception) {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_enter_no_repeat_text))
        }
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

    private fun performStepAfterAd() {
        if (isCopyClicked) {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText(
                    binding.text.text.toString(), binding.result.text.toString()
                )
            )
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.copied_to_clipboard))
        } else {
            finish()
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }
    private fun shareContent() {



        if (binding.result.text.isEmpty()) {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.message_empty))
            return
        }
        val shareIntent = Intent()
        shareIntent.action = "android.intent.action.SEND"
        shareIntent.putExtra(
            "android.intent.extra.TEXT", truncateStringIfExceeds1MB(binding.result.text.toString())
        )
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.type = "text/plain"
        MyAppOpenAd.isDialogClose=false
        resultShareCallback.launch(Intent.createChooser(
            shareIntent, binding.root.context.getString(R.string.intent_text)))
    }

    fun truncateStringIfExceeds1MB(input: String): String {
        var maxSize=input.length
        if(input.length>150000)
        {
            maxSize=150000
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            input.substring(0, maxSize)
        } else {
            input
        }
    }
}