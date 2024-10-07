package dzm.wamr.recover.deleted.messages.photo.media.firebase

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.AllChat.Companion.ACTION_NEW_NOTIFICATION
import dzm.wamr.recover.deleted.messages.photo.media.activities.MainActivity
import dzm.wamr.recover.deleted.messages.photo.media.sqlDb.DatabaseHelper
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {
    //NotificationListenerService
    var context: Context? = null
    var mLastClickTime: Long = 0
    var flag = 0
    var lastMessage: String = ""
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        flag = if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.media_recovery))
            .setContentText(applicationContext.getString(R.string.media_recovery_service_is_running))
            .setSmallIcon(R.mipmap.ic_launcher_round).setContentIntent(pendingIntent).build()



        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(MyApp.CHANNEL_ID, MyApp.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(defaultChannel)
        }

        NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)
    }

    private fun readAllDataFromBundleExtras(bundle: Bundle?): Map<String, Any?> {
        val allData = mutableMapOf<String, Any?>()

        bundle?.keySet()?.forEach { key ->
            allData[key] = bundle.get(key)
        }

        return allData
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        val notification = sbn.notification
        val packageName = sbn.packageName
        val extras: Bundle? = notification.extras
//        checkNotificationData(sbn)
        if (extras != null) {

            val value: CharSequence? = extras.getCharSequence(Notification.EXTRA_TEXT)
            if (value != null) {
                val notificationtext: String = value.toString()
                val notitficationTitle = extras.getString("android.title")
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        val currentTime =
                            SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(Date())
                        val currentDate =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        val sdf = SimpleDateFormat("EEEE")
                        val d = Date()
                        val dayOfTheWeek = sdf.format(d)
                        try {
                            if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
                                val databaseHelper = DatabaseHelper(context)
                                if (notitficationTitle != null && notificationtext != null) { // do not save these messages in database as these are application generated messages
                                    if (notitficationTitle.equals(
                                            WHATSAPP, ignoreCase = true
                                        ) || notitficationTitle.equals(
                                            WHATSAPP_WEB, ignoreCase = true
                                        ) || notitficationTitle.equals(
                                            FINISHED_BACKUP, ignoreCase = true
                                        ) || notitficationTitle.equals(
                                            BACKUP_PROGRESS, ignoreCase = true
                                        ) || notitficationTitle.equals(
                                            BACKUP_PAUSED, ignoreCase = true
                                        ) || notitficationTitle.equals(
                                            YOU, ignoreCase = true
                                        ) || notificationtext.equals(
                                            CHECKING_NEW_MESSAGE, ignoreCase = true
                                        ) || notificationtext.equals(
                                            CHECKING_WEB_LOGIN, ignoreCase = true
                                        ) || notificationtext.equals(
                                            BACKUP_INFO, ignoreCase = true
                                        ) || notificationtext.equals(
                                            WAITING_FOR_WIFI, ignoreCase = true
                                        ) || notificationtext.equals(
                                            RESTORE_MEDIA, ignoreCase = true
                                        ) || notificationtext.equals(
                                            MESSAGE_ONGOING_VOICE_CALL, ignoreCase = true
                                        ) || notificationtext.contains(
                                            MESSAGE_MISSED_VOICE_CALL, ignoreCase = true
                                        ) || notificationtext.equals(
                                            MESSAGE_CALLING, ignoreCase = true
                                        ) || notificationtext.equals(
                                            MESSAGE_RINGING, ignoreCase = true
                                        ) || notificationtext.contains(
                                            NEW_MESSAGES,
                                            ignoreCase = true
                                        ) || notificationtext.equals(
                                            lastMessage,
                                            ignoreCase = true
                                        ) || notitficationTitle.contains(
                                            VIDEO_CALLS,
                                            ignoreCase = true
                                        ) || notitficationTitle.contains(
                                            MESSAGE_MISSED_VOICE_CALL, ignoreCase = true
                                        )
                                    ) {
                                        //do nothing
                                    } else {
                                        if (notificationtext.contains("Photo", ignoreCase = true)) {
                                            lastMessage = ""
                                        } else if (notificationtext.contains(
                                                "Video", ignoreCase = true
                                            )
                                        ) {
                                            lastMessage = ""
                                        } else if (notificationtext.contains(
                                                "Voice message", ignoreCase = true
                                            )
                                        ) {
                                            lastMessage = ""
                                        } else {
                                            lastMessage = notificationtext
                                        }
                                        try {
                                            if (notitficationTitle.contains("(")) {
                                                val group = notitficationTitle.substring(
                                                    notitficationTitle.lastIndexOf(
                                                        "("
                                                    ) - 1
                                                )
                                                val gr = notitficationTitle.substring(
                                                    notitficationTitle.lastIndexOf(
                                                        ":"
                                                    ) + 1
                                                )
                                                val grp = notitficationTitle.replace(
                                                    group.trim { it <= ' ' }, ""
                                                )
                                                databaseHelper.insertuserdata(
                                                    grp.trim(),
                                                    notificationtext,
                                                    currentDate.toString(),
                                                    currentTime.toString(),
                                                    " ",
                                                    "group",
                                                    gr
                                                )
                                            } else if (notitficationTitle.contains(":")) {
                                                val gr = notitficationTitle.substring(
                                                    notitficationTitle.lastIndexOf(
                                                        ":"
                                                    ) + 1
                                                )
                                                val name = notitficationTitle.substring(
                                                    notitficationTitle.lastIndexOf(
                                                        ":"
                                                    )
                                                )
                                                val grp = notitficationTitle.replace(
                                                    name.trim { it <= ' ' }, ""
                                                )
                                                databaseHelper.insertuserdata(
                                                    grp.trim(),
                                                    notificationtext,
                                                    currentDate,
                                                    currentTime,
                                                    "",
                                                    "group",
                                                    gr
                                                )
                                            } else {
                                                databaseHelper.insertuserdata(
                                                    notitficationTitle,
                                                    notificationtext,
                                                    currentDate,
                                                    currentTime,
                                                    " ",
                                                    "chat",
                                                    ""
                                                )
                                            }
                                        } catch (ignored: Exception) {
                                        }
                                    }
                                    val msgrcv = Intent(ACTION_NEW_NOTIFICATION)
                                    msgrcv.putExtra("isShow",true)
                                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(msgrcv)
                                }
                            }
                        }catch (e:Exception){}
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
//        sbn?.let {
//            checkNotificationData(it)
//        }

    }

    private fun checkNotificationData(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val extras: Bundle? = notification?.extras
        Log.e(TAG, "" + packageName)
        if (extras != null) {



            val allData: Map<String, Any?> = readAllDataFromBundleExtras(extras)

            // Iterate over the map and use the data as needed
            var titleNoti = ""
            var messageNoti = ""
            var lastIdNoti = ""
            for ((key, value) in allData) {
//                Log.e(TAG, "Key-> " +key)
//                Log.e(TAG, "Value-> " +value)
                if (("${key}").equals("android.title"))
                {
                    titleNoti="${value}"
                }
                if (("${key}").equals("android.text"))
                {
                    messageNoti="${value}"
                }
                if (("${key}").equals("last_row_id"))
                {
                    lastIdNoti="${value}"
                }
            }

            Log.e(TAG, "================================================================")
            Log.e(TAG, "" + titleNoti +"\n" + messageNoti+"\n" + lastIdNoti)
            Log.e(TAG, "================================================================")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}