package dzm.wamr.recover.deleted.messages.photo.media.util

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.*

class ContextWrapper(base: Context?) : android.content.ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale?): ContextWrapper {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                var localeList: LocaleList? = null
                localeList = LocaleList(newLocale)

//            LocaleList.setDefault(localeList);
                configuration.setLocales(localeList)
                context = context.createConfigurationContext(configuration)
            }
            return ContextWrapper(context)
        }
    }
}