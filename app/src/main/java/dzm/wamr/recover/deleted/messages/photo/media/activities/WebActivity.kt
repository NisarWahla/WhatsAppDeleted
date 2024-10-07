package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityWebBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.AppCompatLocaleActivity
import dzm.wamr.recover.deleted.messages.photo.media.util.Const
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils

class WebActivity : AppCompatLocaleActivity() {
    lateinit var binding: ActivityWebBinding
    var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web)
        initControl()
    }

    private fun initControl() {
        initBrowser()
    }

    private fun initBrowser() {
        binding.web.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 80) {
                    binding.progress.setVisibility(View.GONE)
                }
            }
        })
        binding.web.clearFormData()
        binding.web.settings.saveFormData = true
        binding.web.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0"
        binding.web.settings.allowFileAccess = true
        binding.web.settings.javaScriptEnabled = true
        binding.web.settings.defaultTextEncodingName = "UTF-8"
        binding.web.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.web.settings.databaseEnabled = true
        binding.web.settings.builtInZoomControls = false
        binding.web.settings.setSupportZoom(false)
        binding.web.settings.useWideViewPort = true
        binding.web.settings.domStorageEnabled = true
        binding.web.settings.allowFileAccess = true
        binding.web.settings.loadWithOverviewMode = true
        binding.web.settings.loadsImagesAutomatically = true
        binding.web.settings.blockNetworkImage = false
        binding.web.settings.blockNetworkLoads = false
        binding.web.settings.loadWithOverviewMode = true
        binding.web.loadUrl("https://web.whatsapp.com/%F0%9F%8C%90/${Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE)}")
        binding.web.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
//                if (url.equals("closePopup", ignoreCase = true)) {
//                    onBackPressed()
//                }
                return false
            }
        })
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
       Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_press_again_to_exit))
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

}