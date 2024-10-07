package dzm.wamr.recover.deleted.messages.photo.media.util

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import java.util.*

open class AppCompatLocaleActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val languageArray = newBase.resources.getStringArray(R.array.app_language_code)
        val languageCode = Arrays.asList(*languageArray)
        val language = Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && languageCode.contains(language)) {
            val newLocale = Locale(language)
            super.attachBaseContext(ContextWrapper.wrap(newBase, newLocale))
        } else {
            super.attachBaseContext(newBase)
        }
    }
}