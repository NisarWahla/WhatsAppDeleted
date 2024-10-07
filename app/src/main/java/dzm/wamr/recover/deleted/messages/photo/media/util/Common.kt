package dzm.wamr.recover.deleted.messages.photo.media.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.StatusFragment
import dzm.wamr.recover.deleted.messages.photo.media.model.MediaModel
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*


class Common {
    companion object {
        val GRID_COUNT = 2
        val STATUS_LIS = "list"
        private val CHANNEL_NAME = "GAUTHAM"
        const val ACTION_TYPE = "type"
        const val ACTION_DELETE = "delete"
        const val ACTION_IMAGE_DELETE  = 1
        const val ACTION_VIDEO_DELETE  = 2
        const val ACTION_DOC_DELETE  = 3
        const val ACTION_AUDIO_DELETE  = 4
        const val ACTION_PATH = "path";
        val STATUS_DIRECTORY = File(
            Environment.getExternalStorageDirectory().toString() +
                    File.separator + "WhatsApp/Media/.Statuses"
        )

        val STATUS_DIRECTORY_NEW = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp"
        val STATUS_DIRECTORY_NEW_business = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business"

        var APP_DIR: String? = File(
            Environment.getExternalStorageDirectory(),
            "RecoveryApp${File.separator}WhatsRecovery Statuses"
        ).path


         fun copySingleFile(sourceFile: File, destFile: File) {
            System.out.println(
                "COPY FILE: "
            )
            Log.d("CopyFiles", "copySingleFile: "+ sourceFile.absolutePath
                    .toString() + " TO: " + destFile.absolutePath)
            val file = destFile.absolutePath + "/${sourceFile.name}"
            val file1 = File(file)
            if (!file1.exists()) {
                file1.createNewFile()
            }
            var sourceChannel: FileChannel? = null
            var destChannel: FileChannel? = null
            try {
                sourceChannel = FileInputStream(sourceFile).channel
                destChannel = FileOutputStream(file1).channel
                sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
            } finally {
                sourceChannel?.close()
                destChannel?.close()
            }
        }


        fun copyFile( status: Status, context: Context) {
//            val file = File(Environment.getExternalStorageDirectory(), "RecoveryApp${File.separator}WhatsRecovery Statuses"
//            )
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "RecoveryApp${File.separator}WhatsRecovery Statuses"
            )

            if (!file.exists()) {
                file.mkdirs()
                if (!file.exists()) {
                    Toast.makeText(context,context.getString(R.string.please_reload_again),Toast.LENGTH_SHORT).show()
//                    Snackbar.make(container, getString(R.string.please_reload_again), Snackbar.LENGTH_SHORT).show()
                }
            }
            //            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
//            val currentDateTime = sdf.format(Date())
            val fileName: String = if (status.isVideo) {
                "${status.title}.mp4"
            } else {
                "${status.title}.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            try {
                FileUtils.copyFile(status.file, destFile)
                SingleMediaScanner().SingleMediaScanner(context, file, null, null, null)
                StatusFragment.RefreshDownloads()
                showNotification(context, destFile, status)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun copySingleFileesMedia(sourceFile: File, destFile: File) {
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            var sourceChannel: FileChannel? = null
            var destChannel: FileChannel? = null
            try {
                sourceChannel = FileInputStream(sourceFile).channel
                destChannel = FileOutputStream(destFile).channel
                sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
            } finally {
                if (sourceChannel != null) {
                    sourceChannel.close()
                }
                if (destChannel != null) {
                    destChannel.close()
                }
            }
        }

        fun copyMediaFile(mediaImg: MediaModel, context: Context, container: RelativeLayout) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "RecoveryApp${File.separator}WhatsRecovery Statuses"
            )
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Toast.makeText(context,context.getString(R.string.please_reload_again),Toast.LENGTH_SHORT).show()
//                    Snackbar.make(container, getString(R.string.please_reload_again), Snackbar.LENGTH_SHORT).show()
                }
            }
            val fileName: String
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())
            fileName = if (mediaImg.extension == "mp4" || mediaImg.extension == "video/mp4") {
                "VID_$currentDateTime.mp4"
            } else {
                "IMG_$currentDateTime.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            val srcFile = File(mediaImg.uri?.path!!)
            try {
                FileUtils.copyFile(srcFile, destFile)
                SingleMediaScanner().SingleMediaScanner(context, file, null, null, null)
                StatusFragment.RefreshDownloads()
                showMediaNotification(context, container, destFile, mediaImg)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        fun copyFileFromUri(
            status: Status,
            context: Context,

            bitmap: Bitmap?,
            title:String,
            appDir11: String
        ) {
            val inputStream: InputStream
            val outputStream: OutputStream
            val file = File(appDir11)
            val fileName: String
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

            fileName = if (status.isVideo) {
                "$title.mp4"
            } else {
                "$title.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            try {
                val content = context.contentResolver
                inputStream = content.openInputStream(status.fileUri!!)!!
                outputStream = FileOutputStream(destFile)
                Log.i("Download Status: ", "Output Stream Opened successfully")
                val buffer = ByteArray(1000)
                while (inputStream.read(buffer, 0, buffer.size) >= 0) {
                    outputStream.write(buffer, 0, buffer.size)
                }
                SingleMediaScanner().SingleMediaScanner(context, null, status, fileName, bitmap)
                Utils.showToast(context, context.getString(R.string.saved))
                showNotification11(context, destFile, status, appDir11)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun copyStatusesFileFromUri(
            status: Status,
            context: Context,

            bitmap: Bitmap?,
            title:String,
            appDir11: String
        ) {
            val inputStream: InputStream
            val outputStream: OutputStream
            val file = File(appDir11)
            val fileName: String
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

//            fileName = if (status.isVideo) {
//                "$title.mp4"
//            } else {
//                "$title.jpg"
//            }
            fileName = title
            val destFile = File(file.toString() + File.separator + fileName)
            try {
                val content = context.contentResolver
                inputStream = content.openInputStream(status.fileUri!!)!!
                outputStream = FileOutputStream(destFile)
                Log.i("Download Status: ", "Output Stream Opened successfully")
                val buffer = ByteArray(1000)
                while (inputStream.read(buffer, 0, buffer.size) >= 0) {
                    outputStream.write(buffer, 0, buffer.size)
                }
                SingleMediaScanner().SingleMediaScanner(context, null, status, fileName, bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
         fun checkImageValue(status: Status, ppDir11: String): Boolean {
//            val dir = "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/WallCraft/$slug"
//        } else {
//            "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/WallCraft/$slug"
////            Environment.DIRECTORY_DOWNLOADS,
////            "WallCraft/$slug"
//        }
             val fileName: String = if (status.isVideo) {
                 "${status.title}.mp4"
             } else {
                 "${status.title}.jpg"
             }
             val file1 = File(ppDir11)
            val file = File(file1.toString()+File.separator+ fileName)
            return file.exists()
        }
        fun getFileFromUri(
            status: Status,
            context: Context,

            bitmap: Bitmap?,
            title: String,
            appDir11: String
        ): File {
            val inputStream: InputStream
            val outputStream: OutputStream
            val file = File(appDir11)
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

            val fileName: String = if (status.isVideo) {
                "$title.mp4"
            } else {
                "$title.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            try {
                val content = context.contentResolver
                inputStream = content.openInputStream(status.fileUri!!)!!
                outputStream = FileOutputStream(destFile)
//                Log.i("Download Status: ", "Output Stream Opened successfully")
                val buffer = ByteArray(1000)
                while (inputStream.read(buffer, 0, buffer.size) >= 0) {
                    outputStream.write(buffer, 0, buffer.size)
                }
                SingleMediaScanner().SingleMediaScanner(context, null, status, fileName, bitmap)
//                if (container!=null) {
//                    showNotification11(context, container, destFile, status, appDir11)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            destFile.delete()
            return destFile
        }

        fun copyMediaFileFromUri(
            mediaModel: MediaModel,
            context: Context,
            container: RelativeLayout?,
            bitmap: Bitmap?,
            appDir11: String
        ) {
            val inputStream: InputStream
            val outputStream: OutputStream
            val file: File = File(appDir11)
            val fileName: String
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())
            fileName = if (mediaModel.extension == "mp4" || mediaModel.extension == "video/mp4") {
                "VID_$currentDateTime.mp4"
            } else {
                "IMG_$currentDateTime.jpg"
            }
            val destFile = File(file.toString() + File.separator + fileName)
            try {
                val content = context.contentResolver
                inputStream = content.openInputStream(mediaModel.uri!!)!!
                outputStream = FileOutputStream(destFile)
                val buffer = ByteArray(1000)
                while (inputStream.read(buffer, 0, buffer.size) >= 0) {
                    outputStream.write(buffer, 0, buffer.size)
                }
                SingleMediaScanner().SingleMediaModelScanner(context, null, mediaModel, fileName, bitmap)
                if (container!=null) {
                    showNotification11Media(context, container, destFile, mediaModel, appDir11)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun showMediaNotification(
            context: Context,
            container: RelativeLayout,
            destFile: File,
            mediaModel: MediaModel
        ) {
            makeNotificationChannel(context)
            val data = FileProvider.getUriForFile(
                context,
                "dzm.wamr.recover.deleted.messages.photo.media" + ".provider",
                File(destFile.absolutePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            if (mediaModel.extension == "mp4" || mediaModel.extension == "video/mp4") {
                intent.setDataAndType(data, "video/*")
            } else {
                intent.setDataAndType(data, "image/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
            notification.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.status_saved))
                .setContentText(context.getString(R.string.status_saved_successfully))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.notify(Random().nextInt(), notification.build())
            Utils.showToast(context, context.getString(R.string.saved))
        }

        private fun showNotification(
            context: Context,

            destFile: File,
            status: Status
        ) {
            makeNotificationChannel(context)
            val data = FileProvider.getUriForFile(
                context,
                "dzm.wamr.recover.deleted.messages.photo.media" + ".provider",
                File(destFile.absolutePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            if (status.isVideo) {
                intent.setDataAndType(data, "video/*")
            } else {
                intent.setDataAndType(data, "image/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
            notification.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.status_saved))
                .setContentText(context.getString(R.string.status_saved_successfully))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.notify(Random().nextInt(), notification.build())
            Utils.showToast(context, context.getString(R.string.saved))
        }

        private fun showNotification11(
            context: Context,

            destFile: File,
            status: Status,
            appDir11: String
        ) {
            makeNotificationChannel(context)
            val data = FileProvider.getUriForFile(
                context,
                "dzm.wamr.recover.deleted.messages.photo.media" + ".provider",
                File(destFile.absolutePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            if (status.isVideo) {
                intent.setDataAndType(data, "video/*")
            } else {
                intent.setDataAndType(data, "image/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
            notification.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.status_saved))
                .setContentText(context.getString(R.string.status_saved_successfully))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.notify(Random().nextInt(), notification.build())
            Utils.showToast(context, context.getString(R.string.saved))
        }
        private fun showNotification11Media(
            context: Context,
            container: RelativeLayout,
            destFile: File,
            mediaModel: MediaModel,
            appDir11: String
        ) {
            makeNotificationChannel(context)
            val data = FileProvider.getUriForFile(
                context,
                "dzm.wamr.recover.deleted.messages.photo.media" + ".provider",
                File(destFile.absolutePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            if (mediaModel.extension == "mp4" || mediaModel.extension == "video/mp4") {
                intent.setDataAndType(data, "video/*")
            } else {
                intent.setDataAndType(data, "image/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
            notification.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.status_saved))
                .setContentText(context.getString(R.string.status_saved_successfully))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager =
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.notify(Random().nextInt(), notification.build())
            Utils.showToast(context, context.getString(R.string.saved))
        }

        private fun makeNotificationChannel(context: Context) {
            val CHANNEL_ID = "default"
            val CHANNEL_NAME = context.getString(R.string.saved)

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val defaultChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                notificationManager?.createNotificationChannel(defaultChannel)
            }
        }


         fun checkPermission(context: Context): Boolean {
            return if (SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val result = ContextCompat.checkSelfPermission(
                    context,
                    READ_EXTERNAL_STORAGE
                )
                val result1 = ContextCompat.checkSelfPermission(
                    context,
                    WRITE_EXTERNAL_STORAGE
                )
                result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
            }
        }


        fun isNetworkAvailable(context: Context): Boolean {

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return networkInfo.isConnected
            }
        }

        fun hideNavigation(activity: Activity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            // This works only for Android 4.4+
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.window.decorView.systemUiVisibility = flags

                // Code below is to handle presses of Volume up or Volume down.
                // Without this, after pressing volume buttons, the navigation bar will
                // show up and won't hide
                val decorView = activity.window.decorView
                decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                    if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
            }
        }
    }
}