package dzm.wamr.recover.deleted.messages.photo.media.util;

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import dzm.wamr.recover.deleted.messages.photo.media.R

object FeedbackUtils {

    fun getDeviceInfo(context: Context): String {
        val sdk = Build.VERSION.SDK_INT      // API Level
        val model = Build.MODEL            // Model
        val brand = Build.BRAND          // Product
        var infoString = ""
        val locale: String =
            try {
                context.resources.configuration.locales.get(0).country
            } catch (ex: Exception) {
                context.resources.configuration.locales.get(0).country
            }

        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName
            infoString += "Application Version: $version\n"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        infoString += "Brand: " + brand + " (" + model + ")\n" +
                "Android API: " + sdk

        if (locale.isNotEmpty()) {
            infoString += "\nCountry: $locale"
        }
        return infoString
    }

    private fun emailSubject(context: Context): String {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName
            val name = getApplicationName(context)
            return "$name - $version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return context.getString(R.string.app_name)
    }

    fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
            stringId
        )
    }

    @JvmStatic
    fun startFeedbackEmail(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("apps@dzinemedia.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject(context))
        intent.putExtra(
            Intent.EXTRA_TEXT,
            """---Detail Information---${getDeviceInfo(context)}""".trimIndent()
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_via)))

    }

    @JvmStatic
    fun giveReview(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("apps@dzinemedia.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject(context))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_via)))
    }

}