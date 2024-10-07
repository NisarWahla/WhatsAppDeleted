package dzm.wamr.recover.deleted.messages.photo.media.FileObserve

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.NewMediaFragment
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import java.io.*
import java.util.*

class mReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "WAMRMediaScannerObserver_"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "onReceived : ${intent.data?.path}")
        val permissionsToRequest: ArrayList<String> = ArrayList()
        permissionsToRequest.clear()
        if (Build.VERSION.SDK_INT >= 33) {
            Log.e(TAG, "onReceived33")
            val permissionRequired = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            for (permission in permissionRequired) {
                if (ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(permission)
                }
            }
            if (permissionsToRequest.isNotEmpty()) {
                Log.e(TAG, "onReceived : Permissions are already granted")
                manipulate(context, intent)
            } else {
                Log.e(TAG, "onReceived : Handle permission request result")
                //Handle permission request result
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e(TAG, "onReceivedQ")
            val permissionRequired = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            for (permission in permissionRequired) {
                if (ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(permission)
                }
            }
            if (permissionsToRequest.isNotEmpty()) {
                //Permissions are already granted
                manipulate(context, intent)
            } else {
                //Handle permission request result
            }
        } else {
            Log.e(TAG, "onReceivedElse")
            val permissionRequired = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            for (permission in permissionRequired) {
                if (ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(permission)
                }
            }
            if (permissionsToRequest.isNotEmpty()) {
                //Permissions are already granted
                manipulate(context, intent)
            } else {
                //Handle permission request result
            }
        }
    }

    private fun manipulate(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MEDIA_SCANNER_FINISHED -> {
                recoverMedia(context, intent)
                Log.e(TAG, "ACTION_MEDIA_SCANNER_FINISHED")
            }
            "android.intent.action.MEDIA_SCANNER_SCAN_FILE" -> {
                recoverMedia(context, intent)
                Log.e(TAG, "MEDIA_SCANNER_SCAN_FILE")
            }
            else -> {
                recoverMedia(context, intent)
                Log.e(TAG, "recoverMedia : Else")
            }
        }
    }

    private fun recoverMedia(context: Context, intent: Intent) {
        val uri = intent.data
        uri?.let {
            handleRecoverMedia(context,it)
        }
    }

    private fun handleRecoverMedia(context: Context,uri: Uri) {
        val whatsAppMediaPath = uri.path!!
        Log.e(TAG, "Path : $whatsAppMediaPath")
        val sourceFile = File(whatsAppMediaPath)
        if (!sourceFile.exists()) {
            return
        }
        if (whatsAppMediaPath.contains("dzm.wamr.recover.deleted.messages.photo.media")) {
            return
        }
        var childFolder = "WhatsApp/Media/WhatsApp Images/"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Images/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Images/")) {
                childFolder = "WhatsApp/Media/WhatsApp Images/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Video/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Video/")) {
                childFolder = "WhatsApp/Media/WhatsApp Video/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Documents/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Documents/")) {
                childFolder = "WhatsApp/Media/WhatsApp Documents/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Audio/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Audio/")) {
                childFolder = "WhatsApp/Media/WhatsApp Audio/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Voice Notes/")) {
                childFolder = "WhatsApp/Media/WhatsApp Voice Notes/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Animated Gifs/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Animated Gifs/")) {
                childFolder = "WhatsApp/Media/WhatsApp Animated Gifs/"
            } else if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Stickers/") ||
                whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Stickers/")) {
                childFolder = "WhatsApp/Media/WhatsApp Stickers/"
            }
        } else {
            if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Images/")) {
                childFolder = "WhatsApp/Media/WhatsApp Images/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Video/")) {
                childFolder = "WhatsApp/Media/WhatsApp Video/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Documents/")) {
                childFolder = "WhatsApp/Media/WhatsApp Documents/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Audio/")) {
                childFolder = "WhatsApp/Media/WhatsApp Audio/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Voice Notes/")) {
                childFolder = "WhatsApp/Media/WhatsApp Voice Notes/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Animated Gifs/")) {
                childFolder = "WhatsApp/Media/WhatsApp Animated Gifs/"
            } else if (whatsAppMediaPath.contains("/storage/emulated/0/WhatsApp/Media/WhatsApp Stickers/")) {
                childFolder = "WhatsApp/Media/WhatsApp Stickers/"
            }
        }
        val destinationDirectory = File(context.getExternalFilesDir(null), childFolder)
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs()
        }
        val destinationFile = File(destinationDirectory, sourceFile.name)
        Log.i(TAG, "recoverMedia:print $destinationFile")
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                if (whatsAppMediaPath.contains("/storage/emulated/0/Download.WhatsDelete/WhatsDelete Images/VID")) {
                    return
                } else if (whatsAppMediaPath.contains("/storage/emulated/0/Download.WhatsDelete/WhatsDelete Images/AUD")) {
                    return
                } else {
                    if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Documents/") ||
                        whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Documents/")) {
                        sourceFile.copyTo(destinationFile, false)
                    } else {
                        sourceFile.copyTo(destinationFile, false)
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (whatsAppMediaPath.contains("com.whatsapp/WhatsApp/Media/WhatsApp Documents/") ||
                        whatsAppMediaPath.contains("com.whatsapp.w4b/WhatsApp/Media/WhatsApp Documents/")) {
                        sourceFile.copyTo(destinationFile, false)
                    } else {
                        sourceFile.copyTo(destinationFile, false)
                    }
                } else {
                    sourceFile.copyTo(destinationFile, false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val intent = Intent(NewMediaFragment.ACTION_NEW_MEDIA)
        intent.putExtra(
            NewMediaFragment.ACTION_NEW_MEDIA, whatsAppMediaPath
        )
        context.sendBroadcast(intent)
    }

}