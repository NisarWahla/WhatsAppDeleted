package dzm.wamr.recover.deleted.messages.photo.media.util

/**
 * Created by Barry Allen .
 * boxforbot@gmail.com
 */
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.documentfile.provider.DocumentFile
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.FragmentCallBack
import java.io.*
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * messages from by WhatsApp application, not to add in database
 */
const val WHATSAPP = "WhatsApp" //sender_name
const val WHATSAPP_WEB = "WhatsApp Web"//sender_name
const val FINISHED_BACKUP = "Finished backup"//sender_name
const val BACKUP_PROGRESS = "Backup in progress"//sender name
const val BACKUP_PAUSED = "Backup paused" //sender name
const val YOU = "You"//sender name
const val RESTORE_MEDIA = "Restoring media"
const val CHECKING_NEW_MESSAGE = "Checking for new messages"//sender message
const val CHECKING_WEB_LOGIN = "WhatsApp Web is currently active"//sender message
const val BACKUP_INFO = "Tap for more info"//sender message
const val WAITING_FOR_WIFI = "Waiting for Wi-Fi"// sender message
const val MESSAGE_DELETED = "This message was deleted"
const val MESSAGE_INCOMING_VOICE_CALL = "Incoming voice call"
const val MESSAGE_ONGOING_VOICE_CALL = "Ongoing voice call"
const val MESSAGE_MISSED_VOICE_CALL = "Missed voice call"
const val MESSAGE_CALLING = "Calling…"
const val MESSAGE_RINGING = "Ringing…"
const val NEW_MESSAGES = "new messages"
const val VIDEO_CALLS = "Missed Calls"


object Utils {



    fun getActualTimeFromTimeStamp(timeStamp: Long): String {
        val dt = Date(timeStamp)
        val sdf = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        return sdf.format(dt)
    }


    fun isImageFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("image")
    }

    fun isVideoFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }

    fun isVideoFile(context: Context?, path: String): Boolean {
        return if (path.startsWith("content")) {
            val fromTreeUri = DocumentFile.fromSingleUri(context!!, Uri.parse(path))
            val mimeType = fromTreeUri!!.type
            mimeType != null && mimeType.startsWith("video")
        } else {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            mimeType != null && mimeType.startsWith("video")
        }
    }

    fun getBack(paramString1: String?, paramString2: String?): String {
        val localMatcher = Pattern.compile(paramString2).matcher(paramString1)
        return if (localMatcher.find()) {
            "${localMatcher.group(1)}"
        } else ""
    }

    fun getStatusDir(context: Context): File {

        val rootFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir("SavedStatus").toString())
        } else {
            File(Environment.getExternalStorageDirectory().path + "/SavedStatus")
        }
        rootFile.mkdirs()
        return rootFile
    }

    fun download(context: Context, sourceFile: String) {
        val file=File(sourceFile)
        val selectedFileName =file.name
        val filestatus=copyFileFromContentUri(context,sourceFile,
            getPrivateAppFolder(context,".MySavedStatus"),selectedFileName)
        if (filestatus==FileExistence.SRC_NOT_EXIST)
        {
            showToast(context,context.getString(R.string.no_status_file_found))
        } else  if (filestatus==FileExistence.COPY_FILE_ALREADY_EXIST)
        {
            showToast(context,context.getString(R.string.image_already_saved))
        } else  if (filestatus==FileExistence.EXCEPTION_FACE)
        {
            showToast(context,context.getString(R.string.media_scan_failed))
        } else
        {
            showToast(context,context.getString(R.string.saved))
        }
    }

    fun appInstalledOrNot(context: Context, uri: String?): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(uri!!, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun addEventToFirebase(event_name: String, value: String) {
        try {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(MyApp.appInstance)
            val params = Bundle()
            params.putString("click", value)
            firebaseAnalytics.logEvent(event_name, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showToast(context: Context,mes:String){
        Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
    }

    fun readAllIntentData(intent: Intent?) {
        if (intent != null) {
            // Get the Bundle from the intent
            val extras: Bundle? = intent.extras

            if (extras != null) {
                // Iterate through all keys in the Bundle
                for (key in extras.keySet()) {
                    val value = extras[key]
                    if (value != null) {
                        // Print or process the key-value pair
                        Log.d(Const.tag,"Intent Data - Key: $key, Value: $value")
                    }
                }
            } else {
                Log.d(Const.tag,"No intent extras found.")
            }
        } else {
            Log.d(Const.tag,"Intent is null.")
        }
    }


    fun getPermissionStatus(activity: Activity, androidPermissionName: String): String {
        if (ContextCompat.checkSelfPermission(activity, androidPermissionName) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionName)) {
                return "blocked"
            }
            return "denied"
        }
        return "granted"
    }

    fun showDoubleButtonAlert(
        context: Context,
        title: String,
        message: String,
        negTitle: String,
        posTitle: String,
        isCancelable: Boolean,
        callBack: FragmentCallBack
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(isCancelable)
        dialog.setContentView(R.layout.show_double_button_new_popup_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvtitle = dialog.findViewById<TextView>(R.id.tvtitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)
        val tvNegative = dialog.findViewById<TextView>(R.id.tvNegative)
        val tvPositive = dialog.findViewById<TextView>(R.id.tvPositive)

        tvtitle.text = title
        tvMessage.text = message
        tvNegative.text = negTitle
        tvPositive.text = posTitle

        tvNegative.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle().apply {
                putBoolean("isShow", false)
            }
            callBack.onResponce(bundle)
        }

        tvPositive.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle().apply {
                putBoolean("isShow", true)
            }
            callBack.onResponce(bundle)
        }

        dialog.show()
    }

    fun showPermissionSetting(context: Context, message: String) {
        showDoubleButtonAlert(
            context,
            context.getString(R.string.permission_alert),
            message,
            context.getString(R.string.cancel),
            context.getString(R.string.settings),
            false,
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if (bundle.getBoolean("isShow", false)) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                }
            }
        )
    }


    fun convertEmoji(emoji: String): String {
        val result: String
        result = try {
            val convertEmojiToInt = emoji.substring(2).toInt(16)
            val var8 = Character.toChars(convertEmojiToInt)
            String(var8)
        } catch (var5: java.lang.Exception) {
            ""
        }
        return result
    }

    fun convertIntoGif(gifId: String): String {
        return Const.GIF_FIRSTPART + gifId + Const.GIF_SECONDPART
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Iterate through the running services to check if the specified service is running
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun getPrivateAppFolder(context: Context): String {
        return context.filesDir.path
    }

    fun getPrivateAppFolder(context: Context,dirName:String): String {
        return context.filesDir.path+"/$dirName"
    }


    fun frescoGifLoad(url: String?, resource: Int, simpleDrawee: SimpleDraweeView): DraweeController {

        val request: ImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
            .build()
        val radious=simpleDrawee.context.resources.getDimension(R.dimen._8sdp)
        val roundingParams: RoundingParams = RoundingParams.fromCornersRadii(radious, radious, radious, radious)

        val controller: DraweeController
        simpleDrawee.hierarchy.setPlaceholderImage(resource)
        simpleDrawee.hierarchy.setFailureImage(resource)
        simpleDrawee.hierarchy.roundingParams=roundingParams

        controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(simpleDrawee.controller)
            .setAutoPlayAnimations(true)
            .build()

        return controller
    }


    fun setLocale(lang: String, context: Activity, className: Class<*>, isRefresh: Boolean) {
        Log.d(Const.tag,"lang: $lang")
        val languageArray = context.resources.getStringArray(R.array.app_language_code)
        val languageCode = languageArray.toList()

        if (languageCode.contains(lang)) {
            val myLocale = Locale(lang)
            val res: Resources = context.baseContext.resources
            val dm: DisplayMetrics = res.displayMetrics
            val conf: Configuration = res.configuration
            conf.setLocale(myLocale)
            res.updateConfiguration(conf, dm)
            context.onConfigurationChanged(conf)

            if (isRefresh) {
                updateActivity(context, className)
            }
        }
    }

    private fun updateActivity(context: Activity, className: Class<*>) {
        val intent = Intent(context, className)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        )
        context.startActivity(intent)
    }

    fun getDefaultLanguageCode(): String {
        val defaultLocale = Locale.getDefault()
        val languageCode = defaultLocale.language
        return "${languageCode}"
    }

    fun isLayoutDirectionRTL(viewGroup: ViewGroup): Boolean {
        return ViewCompat.getLayoutDirection(viewGroup) == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    fun isNumeric(input: String): Boolean {
        // Remove all special characters and spaces from the input string
        val cleanedString = input.replace("[^0-9]".toRegex(), "")

        return try {
            // Try to parse the cleaned string as a Double
            cleanedString.toDouble()
            true
        } catch (e: NumberFormatException) {
            // If an exception occurs, it means the cleaned string is not numeric
            false
        }
    }

    fun isGifFileExtention(filePath: String): Boolean {
        val imageExtensions = arrayOf(
            "gif", "GIF", "gifv","giff","jif",
            "giphy","mp4"
        )

        // Get the file extension from the file path
        val fileExtension = filePath.substringAfterLast('.', "")

        // Check if the file extension is in the list of image extensions
        return imageExtensions.contains(fileExtension.toLowerCase())
    }
    fun isImageFileExtention(filePath: String): Boolean {
        val imageExtensions = arrayOf(
            "jpg", "jpeg", "png", "bmp",
            "webp", "tiff", "svg", "ico", "jfif",
            "pjpeg", "pjp", "svgz", "tga", "targa",
            "jpe", "jif", "jfif-tbn", "djvu", "djv",
            "bmp", "pdf", "eps", "ai", "indd"
        )

        // Get the file extension from the file path
        val fileExtension = filePath.substringAfterLast('.', "")

        // Check if the file extension is in the list of image extensions
        return imageExtensions.contains(fileExtension.toLowerCase())
    }

    fun isVideoFileExtention(filePath: String): Boolean {
        val videoExtensions = arrayOf(
            "mp4", "avi", "mkv", "mov", "wmv",
            "flv", "webm", "3gp", "mpeg", "mpg",
            "m4v", "m2v", "vob", "rm", "rmvb",
            "ts", "divx", "asf", "ogv", "m2ts",
            "mxf", "ogg", "dat", "mod", "mts"
            // Add more video extensions if needed
        )

        // Get the file extension from the file path
        val fileExtension = filePath.substringAfterLast('.', "")

        // Check if the file extension is in the list of video extensions
        return videoExtensions.contains(fileExtension.toLowerCase())
    }

    fun isAudioFileExtention(filePath: String): Boolean {
        val audioExtensions = arrayOf(
            "mp3", "wav", "flac", "aac", "m4a",
            "ogg", "wma", "alac", "aiff", "ape",
            "mid", "midi", "amr", "ac3", "ra",
            "opus", "pcm", "mka", "au", "snd",
            "mpga", "dts", "m4b", "m4p", "m4r"
            // Add more audio extensions if needed
        )

        // Get the file extension from the file path
        val fileExtension = filePath.substringAfterLast('.', "")

        // Check if the file extension is in the list of audio extensions
        return audioExtensions.contains(fileExtension.toLowerCase())
    }

    fun formatTimePlayerTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun getFileNameWithoutExtension(filePath: String): String {
        val file = File(filePath)
        val fileNameWithExtension = file.name
        val dotIndex = fileNameWithExtension.lastIndexOf('.')

        return if (dotIndex == -1 || dotIndex == 0) {
            // No extension or dot is the first character
            fileNameWithExtension
        } else {
            // Return the substring excluding the extension
            fileNameWithExtension.substring(0, dotIndex)
        }
    }


    fun copyFile(sourceFilePath: String, destinationDirectoryPath: String):FileExistence {
        val sourceFile = File(sourceFilePath)
        if (!sourceFile.exists()) {
            // Source file does not exist
            return FileExistence.SRC_NOT_EXIST
        }

        val destinationDir = File(destinationDirectoryPath)
        if (!destinationDir.exists()) {
            // Destination directory does not exist, create it
            destinationDir.mkdirs()
        }

        val destinationFile = File(destinationDir, sourceFile.name)
        if (destinationFile.exists())
        {
            return FileExistence.COPY_FILE_ALREADY_EXIST
        }

        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            // File copied successfully
            println("File copied successfully.")
        } catch (e: Exception) {
            Log.d(Const.tag,"Error occurred while copying file: ${e.message}")
            return FileExistence.EXCEPTION_FACE
        }
        return FileExistence.PROCESS_DONE
    }


    enum class FileExistence{SRC_NOT_EXIST,COPY_FILE_ALREADY_EXIST,EXCEPTION_FACE,PROCESS_DONE}
    fun copyFileFromContentUri(context: Context, srcPath: String, destinationDirectoryPath: String, sourceFileName:String):FileExistence {
        try {

            val contentUri: Uri=Uri.parse(srcPath)
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(contentUri)

            val destinationDir = File(destinationDirectoryPath)
            if (!destinationDir.exists()) {
                // Destination directory does not exist, create it
                destinationDir.mkdirs()
            }

            val destinationFile = File(destinationDir, sourceFileName)
            if (destinationFile.exists())
            {
                return FileExistence.COPY_FILE_ALREADY_EXIST
            }

            inputStream?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            // File copied successfully
            return FileExistence.PROCESS_DONE

        }catch (e:Exception)
        {
            Log.d(Const.tag,"Exception: $e")
            return FileExistence.EXCEPTION_FACE
        }
    }
}