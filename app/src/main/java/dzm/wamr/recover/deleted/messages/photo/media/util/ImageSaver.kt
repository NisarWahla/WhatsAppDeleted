package dzm.wamr.recover.deleted.messages.photo.media.util

import android.app.Activity
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import dzm.wamr.recover.deleted.messages.photo.media.R
import java.io.File
import java.io.FileOutputStream

class ImageSaver(private val context: Activity, isSecure: Boolean) {
    private var directoryName = "images"
    private var fileName = "image.png"
    var isSecure = false

    init {
        this.isSecure = isSecure
    }

    fun setFileName(fileName: String): ImageSaver {
        this.fileName = fileName
        return this
    }

    fun setDirectoryName(directoryName: String): ImageSaver {
        this.directoryName = directoryName
        return this
    }

    fun save(bitmapImage: Bitmap) {
        var fileOutputStream: FileOutputStream? = null
        val outPutFile = createFile()
        try {
            fileOutputStream = FileOutputStream(outPutFile)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        MediaScannerConnection.scanFile(
            context, arrayOf<String>(outPutFile.absolutePath),
            null
        ) { path, uri ->
            context.runOnUiThread {
                if (!isSecure) Toast.makeText(
                    context,
                    context.getString(R.string.saved_in_downloads),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createFile(): File {
        val directory: File
        if (isSecure) {
            directory = File(Utils.getPrivateAppFolder(context) + "/" + directoryName + "/")
        } else {
            directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + "/" + directoryName + "/"
            )
        }
        if (!directory.exists()) directory.mkdirs()
        return File(directory, fileName)
    }
}