package dzm.wamr.recover.deleted.messages.photo.media.util

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import dzm.wamr.recover.deleted.messages.photo.media.FileObserve.Fuction
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.StatusFragment
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object LoadingDataClass {

    private val mediaListSpecial: ArrayList<Status> = ArrayList()
    val mediaList: ArrayList<Status> = ArrayList()
    private val MP4 = ".mp4"

    @OptIn(DelicateCoroutinesApi::class)

    private fun saveFiles(context: Context, path: Uri?, name: String, savedPath: String) {

        try {
            GlobalScope.launch(Dispatchers.IO) {
                var outputStream: OutputStream? = null
                var inputStream: InputStream? = null
                try {
                    val content = context.contentResolver
                    inputStream = content.openInputStream(path!!)
                    //                    FileUtils.copyInputStreamToFile(inputStream,new File(savedPath + "/" + name));
                    //                    FileInputStream fileInputStream = new FileInputStream(path);
                    outputStream = FileOutputStream("$savedPath/$name")
                    val bArr = ByteArray(1024)
                    while (true) {
                        val read = inputStream!!.read(bArr)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(bArr, 0, read)
                    }
                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                    val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
                    intent.data = Uri.fromFile(File("$savedPath/$name"))
                    context.sendBroadcast(intent)
                    context.sendBroadcast(
                        Intent(
                            "android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(
                                File(
                                    "$savedPath/$name"
                                )
                            )
                        )
                    )
                    null
                } catch (unused: Exception) {
                    Log.d("excion", "doInBackground: $unused")
                    null
                }
            }
        } catch (e: Exception) {

        }


    }

    fun getDataList(): ArrayList<Status> {
        return mediaListSpecial
    }

    private val imagesList: ArrayList<Status> = ArrayList()

    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities != null) {
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        return false
    }

    fun getStatusImages(): ArrayList<Status> {
        return imagesList
    }


    private val videoList: ArrayList<Status> = ArrayList()
    private val audioList: ArrayList<Status> = ArrayList()

    fun getMedia(context: Context) {
        try {
            var WhatsRecovery = "";
            WhatsRecovery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images")!!.absolutePath
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Images"
            }

            GlobalScope.launch(Dispatchers.Default) {

                //val listFiles_one = Fuction.getListFiles_one(context.getExternalFilesDir(".WhatsRecovery"))
                val listFiles_one = Fuction.getListFiles_one(File(WhatsRecovery))

                if (listFiles_one != null) {
                    var i = 0
                    while (i < listFiles_one.size) {
                        try {
                            val file = listFiles_one[i]
                            val uri = Uri.fromFile(file.absoluteFile)
                            val ext = file.name
                            val fileEnd = ext.substring(ext.indexOf(".") + 1)
                            val type = fileEnd == "mp4"
                            mediaList.add(Status(file, file.name, file.absolutePath, type, uri))
                            i++
                        } catch (unused: Exception) {
                        }
                    }
                }

            }
        } catch (e: Exception) {

        }

    }

    fun getImgMedia(context: Context) {
        try {
            var WhatsRecovery = "";
            WhatsRecovery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images")!!.absolutePath
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Images"
            }

            GlobalScope.launch(Dispatchers.Default) {

                //val listFiles_one = Fuction.getListFiles_one(context.getExternalFilesDir(".WhatsRecovery"))
                val listFiles_one = Fuction.getListFiles_one(File(WhatsRecovery))
                //Log.i("TAG", "getImgMedia: " + listFiles_one.size)
                if (listFiles_one != null && listFiles_one.size > 0) {
                    var i = 0
                    while (i < listFiles_one.size) {
                        Log.i("TAG", "getImgMedia: $i")
                        try {
                            val file = listFiles_one[i]
                            val uri = Uri.fromFile(file.absoluteFile)
                            val ext = file.name
                            val fileEnd = ext.substring(ext.indexOf(".") + 1)
                            val type = fileEnd == "mp4"
                            val status = Status(
                                File(file.path), file?.name, file?.path, type, file.toUri()
                            )
                            if (status.title!!.contains("IMG")) {
                                Log.i("TAG", "statusImages: " + status.path)
                                imagesList.add(status)
                            }
                            i++
                        } catch (unused: Exception) {
                        }
                    }
                }

            }
        } catch (e: Exception) {

        }

    }

    fun getVideoMedia(context: Context) {

        try {
            var WhatsRecovery = "";
            WhatsRecovery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images")!!.absolutePath
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Images"
            }

            GlobalScope.launch(Dispatchers.Default) {

                //val listFiles_one = Fuction.getListFiles_one(context.getExternalFilesDir(".WhatsRecovery"))
                val listFiles_one = Fuction.getListFiles_one(File(WhatsRecovery))

                if (listFiles_one != null) {
                    var i = 0
                    while (i < listFiles_one.size) {
                        try {
                            val file = listFiles_one[i]
                            val uri = Uri.fromFile(file.absoluteFile)
                            val ext = file.name
                            val fileEnd = ext.substring(ext.indexOf(".") + 1)
                            val type = fileEnd == "mp4"
                            val status = Status(
                                File(file.path), file?.name, file?.path, type, file.toUri()
                            )
                            if (status.title!!.contains("VID")) {
                                videoList.add(status)
                            }
                            i++
                        } catch (unused: Exception) {
                        }
                    }
                }

            }
        } catch (e: Exception) {

        }

    }

    fun getAudioMedia(context: Context) {
        try {
            audioList.clear()
            var WhatsRecovery = ""
            WhatsRecovery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images")!!.absolutePath
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Images"
            }

            GlobalScope.launch(Dispatchers.Default) {

                //val listFiles_one = Fuction.getListFiles_one(context.getExternalFilesDir(".WhatsRecovery"))
                val listFiles_one = Fuction.getListFiles_one(File(WhatsRecovery))

                if (listFiles_one != null) {
                    var i = 0
                    while (i < listFiles_one.size) {
                        try {
                            val file = listFiles_one[i]
                            val uri = Uri.fromFile(file.absoluteFile)
                            val ext = file.name
                            val fileEnd = ext.substring(ext.indexOf(".") + 1)
                            val type = fileEnd == "mp4"
                            val status = Status(
                                File(file.path), file?.name, file?.path, type, file.toUri()
                            )
                            if (status.title!!.contains("AUD")) {
                                audioList.add(status)
                            }
                            i++
                        } catch (unused: Exception) {
                        }
                    }
                }

            }
        } catch (e: Exception) {

        }

    }

    fun getMediaData(): ArrayList<Status> {
        return mediaList
    }

    fun getAudioData(): ArrayList<Status> {
        return audioList
    }

    fun getVideoData(): ArrayList<Status> {
        return videoList
    }

}