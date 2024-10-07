package dzm.wamr.recover.deleted.messages.photo.media.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.model.MediaModel
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import java.io.File

class SingleMediaScanner {
    private val TAG = "Media Scanner Client: "

    @SuppressLint("NotConstructor")
    fun SingleMediaScanner(
        context: Context,
        f: File?,
        status: Status?,
        fileName: String?,
        bitmap: Bitmap?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (status == null || fileName == null) {
                Log.e(TAG, "No Filename/Bitmap Provided")
                return
            }

            // To Notify Media Scanner
            var url: Uri? = null
            val cr = context.contentResolver
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.DESCRIPTION, fileName)
            if (status.isVideo) {
                values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
            } else {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            try {
                url = if (status.isVideo) {
                    cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                } else {
                    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                }
                if (bitmap != null) {
                    cr.openOutputStream(url!!).use { imageOut ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            50,
                            imageOut!!
                        )
                    }
                } else {
                    cr.delete(url!!, null, null)
                }
                cr.delete(url, null, null)
            } catch (e: Exception) {
                if (url != null) {
                    cr.delete(url, null, null)
                }
                e.printStackTrace()
            }
            return
        }
        if (f == null) {
            Log.e(TAG, "No File Provided")
            return
        }
        MediaScannerConnection.scanFile(
            context, arrayOf(f.absolutePath), arrayOf("image/jpeg", "videos/mp4")
        ) { path: String?, uri: Uri? ->
            checkNotNull(uri) { "media scan failed..." }
            Log.i(TAG, "Success")
        }
    }
    fun SingleMediaModelScanner(
        context: Context,
        f: File?,
        mediaModel: MediaModel?,
        fileName: String?,
        bitmap: Bitmap?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mediaModel == null || fileName == null) {
                Log.e(TAG, "No Filename/Bitmap Provided")
                return
            }

            // To Notify Media Scanner
            var url: Uri? = null
            val cr = context.contentResolver
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.DESCRIPTION, fileName)
            if (mediaModel.extension == "mp4" || mediaModel.extension == "video/mp4") {
                values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
            } else {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            try {
                url = if (mediaModel.extension == "mp4" || mediaModel.extension == "video/mp4") {
                    cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                } else {
                    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                }
                if (bitmap != null) {
                    cr.openOutputStream(url!!).use { imageOut ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            50,
                            imageOut!!
                        )
                    }
                } else {
                    cr.delete(url!!, null, null)
                }
                cr.delete(url, null, null)
            } catch (e: Exception) {
                if (url != null) {
                    cr.delete(url, null, null)
                }
                e.printStackTrace()
            }
            return
        }
        if (f == null) {
            Log.e(TAG, "No File Provided")
            return
        }
        MediaScannerConnection.scanFile(
            context, arrayOf(f.absolutePath), arrayOf("image/jpeg", "videos/mp4")
        ) { path: String?, uri: Uri? ->
            checkNotNull(uri) { context.getString(R.string.media_scan_failed) }
            Log.i(TAG, "Success")
        }
    }
}