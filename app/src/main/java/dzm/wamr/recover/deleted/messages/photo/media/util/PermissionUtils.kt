package dzm.wamr.recover.deleted.messages.photo.media.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.FragmentCallBack

class PermissionUtils(
    var activity: Activity,
    var mPermissionResult: ActivityResultLauncher<Array<String>>
) {


    fun showStoragePermissionDailog(message: String) {
        val permissionStatusList: MutableList<String> = ArrayList()
        val permissions: Array<String>
        permissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        for (keyStr in permissions) {
            permissionStatusList.add(Utils.getPermissionStatus(activity, keyStr))
        }
        if (permissionStatusList.contains("denied")) {
            Utils.showDoubleButtonAlert(
                activity,
                activity.getString(R.string.permission_alert),
                message,
                activity.getString(R.string.cancel),
                activity.getString(R.string.permission),
                false,
                object : FragmentCallBack{
                    override fun onResponce(bundle: Bundle) {
                        if (bundle.getBoolean("isShow", false)) {
                            takeStoragePermission()
                        }
                    }
                })
            return
        }
        takeStoragePermission()
    }

    fun takeStoragePermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            val permissions =
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
            mPermissionResult.launch(permissions)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            mPermissionResult.launch(permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            mPermissionResult.launch(permissions)
        }
    }

    fun isStoragePermissionGranted(): Boolean{
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            val mediaImagesPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_IMAGES
            )
            val mediaVideoPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_VIDEO
            )
            val mediaAudioPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_AUDIO
            )
            mediaImagesPermission == PackageManager.PERMISSION_GRANTED && mediaVideoPermission == PackageManager.PERMISSION_GRANTED && mediaAudioPermission == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val readExternalStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            readExternalStoragePermission == PackageManager.PERMISSION_GRANTED
        } else {
            val readExternalStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            readExternalStoragePermission == PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED
        }
    }



    fun showNotificationPermissionDailog(message: String) {
        val permissionStatusList: MutableList<String> = ArrayList()
        val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        for (keyStr in permissions) {
            permissionStatusList.add(Utils.getPermissionStatus(activity, keyStr))
        }
        if (permissionStatusList.contains("denied")) {
            Utils.showDoubleButtonAlert(
                activity,
                activity.getString(R.string.permission_alert),
                message,
                activity.getString(R.string.cancel),
                activity.getString(R.string.permission),
                false,
                object : FragmentCallBack{
                    override fun onResponce(bundle: Bundle) {
                        if (bundle.getBoolean("isShow", false)) {
                            takeNotificationPermission()
                        }
                    }
                })
            return
        }
        takeNotificationPermission()
    }

    fun takeNotificationPermission() {
        val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        mPermissionResult.launch(permissions)
    }

    fun isNotificationPermissionGranted(): Boolean{
        val notificaionPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.POST_NOTIFICATIONS)
        return notificaionPermission == PackageManager.PERMISSION_GRANTED
    }


    fun showForegroundPermissionDailog(message: String) {
        val permissionStatusList: MutableList<String> = ArrayList()
        val permissions = arrayOf(Manifest.permission.FOREGROUND_SERVICE)
        for (keyStr in permissions) {
            permissionStatusList.add(Utils.getPermissionStatus(activity, keyStr))
        }
        if (permissionStatusList.contains("denied")) {
            Utils.showDoubleButtonAlert(
                activity,
                activity.getString(R.string.permission_alert),
                message,
                activity.getString(R.string.cancel),
                activity.getString(R.string.permission),
                false,
                object : FragmentCallBack{
                    override fun onResponce(bundle: Bundle) {
                        if (bundle.getBoolean("isShow", false)) {
                            takeNotificationPermission()
                        }
                    }
                })
            return
        }
        takeNotificationPermission()
    }

    fun takeForegroundPermission() {
        val permissions = arrayOf(Manifest.permission.FOREGROUND_SERVICE)
        mPermissionResult.launch(permissions)
    }

    fun isForegroundPermissionGranted(): Boolean{
        val foregroundPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.FOREGROUND_SERVICE)
        return foregroundPermission == PackageManager.PERMISSION_GRANTED
    }
}