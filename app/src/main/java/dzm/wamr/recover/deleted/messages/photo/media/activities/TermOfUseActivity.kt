package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.BuildConfig
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityTermOfUseBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class TermOfUseActivity : AppCompatLocaleActivity() {
    lateinit var binding: ActivityTermOfUseBinding
    lateinit var prefManager:PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_term_of_use)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.tvAccept.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            if (binding.checkbox.isChecked) {
                prefManager.setAcceptTermOfUse(true, BuildConfig.VERSION_NAME)
                if (!prefManager.isFirstTimeLaunch()) {
                    val callingIntent=Intent(binding.root.context, SplashA::class.java)
                    callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(callingIntent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                } else {
                    val callingIntent=Intent(binding.root.context, WelcomeActivity::class.java)
                    callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(callingIntent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                }
            } else {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.tick_checkbox))
            }
        }))
    }

    private fun initControl() {
        prefManager = PrefManager(binding.root.context)
        binding.webView.loadUrl("file:///android_asset/term.html")
    }
}