package dzm.wamr.recover.deleted.messages.photo.media.FileObserve;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import dzm.wamr.recover.deleted.messages.photo.media.R;
import dzm.wamr.recover.deleted.messages.photo.media.activities.MainActivity;

public class NewFileDetection extends Service {
    private static final String FOREGROUND_CHANNEL_ID = "foreground_channel_id";

    RecursiveFileObserverForAudios recursiveFileObserverForAudios;
    RecursiveFileObserverForDoc recursiveFileObserverForDoc;
    RecursiveFileObserverForVideos recursiveFileObserverForVideos;
    private NotificationManager mNotificationManager;
    Context context;

    private Notification prepareNotification() {


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (this.mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
                NotificationChannel defaultChannel = new NotificationChannel(FOREGROUND_CHANNEL_ID, context.getString(R.string.media_observer_service), NotificationManager.IMPORTANCE_DEFAULT);
                defaultChannel.enableVibration(false);
                this.mNotificationManager.createNotificationChannel(defaultChannel);
            }
        }
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        builder.setContentTitle(getString(R.string.media_recovery)).setContentText(getString(R.string.media_recovery_service_is_running)).setSmallIcon(R.mipmap.ic_launcher_round).setCategory(NotificationCompat.CATEGORY_SERVICE).setOnlyAlertOnce(true).setOngoing(true).setPriority(-2).setAutoCancel(true).setContentIntent(activity);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder.build();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        try {
            context = this;
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            startForeground(8416303, prepareNotification());
        } catch (Exception ignored) {

        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            try {

                if (recursiveFileObserverForVideos != null) {
                    recursiveFileObserverForVideos.stopWatching();
                }
                if (recursiveFileObserverForAudios != null) {
                    recursiveFileObserverForAudios.stopWatching();
                }
                if (recursiveFileObserverForDoc != null) {
                    recursiveFileObserverForDoc.stopWatching();
                }
            } catch (Exception e) {

            }
            String docPath, audioPath, videosPath, imagesPath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                docPath = Environment.getStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/";
                audioPath = Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/";
                videosPath = Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video/";
                imagesPath = Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/";

            } else {
                docPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Documents/";
                audioPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Audio/";
                videosPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Video/";
                imagesPath = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images/";

            }
            try {
                recursiveFileObserverForAudios = new RecursiveFileObserverForAudios(audioPath, context);
                recursiveFileObserverForAudios.startWatching();
                recursiveFileObserverForVideos = new RecursiveFileObserverForVideos(videosPath, context);
                recursiveFileObserverForVideos.startWatching();
                recursiveFileObserverForDoc = new RecursiveFileObserverForDoc(imagesPath, context);
                recursiveFileObserverForDoc.startWatching();
            } catch (Exception ignored) {

            }

        }).start();

        try {
            if (intent != null) {
                new Thread(() -> ContextCompat.startForegroundService(context, intent)).start();
            }
            return super.onStartCommand(intent, flags, startId);
        } catch (Exception ignored) {
            return super.onStartCommand(intent, flags, startId);
        }

    }


//    public void onTaskRemoved(Intent intent) {
//        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, PendingIntent.getService(this, 1, new Intent(getApplicationContext(), NewFileDetection.class), PendingIntent.FLAG_IMMUTABLE));
//        super.onTaskRemoved(intent);
//    }
}
