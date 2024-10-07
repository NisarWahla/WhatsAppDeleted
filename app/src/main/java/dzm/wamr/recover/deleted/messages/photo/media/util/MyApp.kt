package dzm.wamr.recover.deleted.messages.photo.media.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.*
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.util.ImagePipelineConfigUtils.getDefaultImagePipelineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class MyApp :  Application() {

    companion object {
//      these two veriable are use for AB testing experiement
        var isNewPremiumScenarioShow=false
        var isExperimentApply=false
//      ================================================================
        const val CHANNEL_ID = "Recovery App"
        const val CHANNEL_NAME = "Recovery  App service is running"
        lateinit var appInstance: MyApp
        const val buttonClick = "buttonClick"
        var showEvenAd: Int = 0
        private var myAppOpenAd: MyAppOpenAd? = null
        var WhatsDelete: String? = null
        var WhatsRecovery: String? = null
        var Images: String? = null
        var Videos: String? = null
        var Documents: String? = null
        var Audio: String? = null
        var Voice: String? = null
        var Status: String? = null
        fun createDirs(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WhatsDelete = context.getExternalFilesDir(".WhatsDelete")?.absolutePath
                WhatsRecovery = context.getExternalFilesDir(".WhatsRecovery")?.absolutePath
                Images =
                    context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images")?.absolutePath
                Videos =
                    context.getExternalFilesDir(".WhatsDelete/WhatsDelete Videos")?.absolutePath
                Documents =
                    context.getExternalFilesDir(".WhatsDelete/WhatsDelete Documents")?.absolutePath
                Audio = context.getExternalFilesDir(".WhatsDelete/WhatsDelete Audio")?.absolutePath
                Voice =
                    context.getExternalFilesDir(".WhatsDelete/WhatsDelete Voice Notes")?.absolutePath
                Status =
                    context.getExternalFilesDir(".WhatsDelete/WhatsDelete Status")?.absolutePath
            } else {
                WhatsDelete =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete"
                WhatsRecovery =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsRecovery"
                Images =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Images"
                Videos =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Videos"
                Documents =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Documents"
                Audio =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Audio"
                Voice =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Voice Notes"
                Status =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + ".WhatsDelete/WhatsDelete Status"
            }
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    val file = File(WhatsDelete!!)
                    val deletesFile = File(WhatsRecovery!!)
                    val file2 = File(Images!!)
                    val file3 = File(Videos!!)
                    val file4 = File(Documents!!)
                    val file5 = File(Audio!!)
                    val file6 = File(Voice!!)
                    val file7 = File(Status!!)
                    if (!file.exists()) {
                        file.mkdirs()
                    }
                    if (!deletesFile.exists()) {
                        deletesFile.mkdirs()
                    }
                    if (!file2.exists()) {
                        file2.mkdirs()
                    }
                    if (!file3.exists()) {
                        file3.mkdirs()
                    }
                    if (!file4.exists()) {
                        file4.mkdirs()
                    }
                    if (!file5.exists()) {
                        file5.mkdirs()
                    }
                    if (!file6.exists()) {
                        file6.mkdirs()
                    }
                    if (!file7.exists()) {
                        file7.mkdirs()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        MobileAds.initialize(this){
            InterstitialManager.preLoadAllScreenInterstital= InterstitialManager(this)
        }
        initFresco()
        myAppOpenAd = MyAppOpenAd(this)
        appInstance = this
        createNotificationChannel()

    }

    private fun initFresco() {
        try {
            Fresco.initialize(applicationContext,getDefaultImagePipelineConfig(applicationContext))
        }catch (e:Exception)
        {
            Fresco.initialize(applicationContext)
        }

    }


    override fun onTerminate() {
        myAppOpenAd?.let {
            unregisterActivityLifecycleCallbacks(it.getLifecycleCallback())
        }
        super.onTerminate()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
}