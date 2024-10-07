package dzm.wamr.recover.deleted.messages.photo.media.FileObserve;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

import dzm.wamr.recover.deleted.messages.photo.media.R;
import dzm.wamr.recover.deleted.messages.photo.media.util.Common;
import dzm.wamr.recover.deleted.messages.photo.media.util.MyApp;

public class DeleteFileReceiver extends BroadcastReceiver {
    String  deletesFile;
    Context context;
    String fileImg;
    String fileVideo ;
    String fileDoc ;
    String fileAudio;
    String name;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        int type = intent.getIntExtra(Common.ACTION_TYPE, Common.ACTION_IMAGE_DELETE);
        String path = intent.getExtras().getString(Common.ACTION_PATH);
        MyApp.Companion.createDirs(context);
       new Thread(() -> {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

               deletesFile = context.getExternalFilesDir(".WhatsRecovery").getAbsolutePath();
               fileImg = context.getExternalFilesDir(".WhatsDelete/WhatsDelete Images").getAbsolutePath();
               fileVideo =context. getExternalFilesDir(".WhatsDelete/WhatsDelete Videos").getAbsolutePath();
               fileDoc =context. getExternalFilesDir(".WhatsDelete/WhatsDelete Documents").getAbsolutePath();
               fileAudio = context.getExternalFilesDir(".WhatsDelete/WhatsDelete Audio").getAbsolutePath();
           } else {

               deletesFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + ".WhatsRecovery";
               fileImg = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + ".WhatsDelete/WhatsDelete Images";
               fileVideo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + ".WhatsDelete/WhatsDelete Videos";
               fileDoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + ".WhatsDelete/WhatsDelete Documents";
               fileAudio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + ".WhatsDelete/WhatsDelete Audio";
           }
           switch (type) {
               case Common.ACTION_IMAGE_DELETE:
                   File img = new File(path);
                   name = img.getName();
                   Log.d("Deleted", "run: "+name);
                   moveImgToDelete(context, name);
                   break;
               case Common.ACTION_VIDEO_DELETE:
                   File video = new File(path);
                   name = video.getName();
                   moveVideoToDelete(context, name);
                   break;
               case Common.ACTION_DOC_DELETE:

                   File doc = new File(path);
                   name = doc.getName();
                   moveDocToDelete(context, name);
                   break;
               case Common.ACTION_AUDIO_DELETE:
                   File audio = new File(path);
                   name = audio.getName();
                   moveAudioToDelete(context, name);
                   break;
               default:
                   return;
           }
       }).start();
    }

    private void moveAudioToDelete(Context context, String name) {
        File[] listFiles = new File(fileAudio).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (!file.isDirectory() && file.getName().equals(name)) {
                    saveFiles(context, file.getPath(), name, new File(deletesFile).getAbsolutePath());
                }
            }
        }
    }

    private void moveDocToDelete(Context context, String name) {
        File[] listFiles = new File(fileDoc).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (!file.isDirectory() && file.getName().equals(name)) {

                    saveFiles(context, file.getPath(), name, new File(deletesFile).getAbsolutePath());
                }
            }
        }
    }

    private void moveVideoToDelete(Context context, String name) {
        File[] listFiles = new File(fileVideo).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (!file.isDirectory() && file.getName().equals(name)) {
                    saveFiles(context, file.getPath(), name, new File(deletesFile).getAbsolutePath());
                }
            }
        }
    }

    private void moveImgToDelete(Context context, String name) {
        File[] listFiles = new File(fileImg).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (!file.isDirectory() && file.getName().equals(name)) {
                    saveFiles(context, file.getPath(), name, new File(deletesFile).getAbsolutePath());
                }
            }
        }
    }

    @SuppressLint({"StaticFieldLeak"})
    private void saveFiles(Context context, String path, String name, String savedPath) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            /* renamed from: a */
            public Void doInBackground(Void... voidArr) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(savedPath + "/" + name);
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = fileInputStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    //                        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
//                        intent.setData(Uri.fromFile(new File(savedPath + "/" + name)));
//                        context.sendBroadcast(intent);
                    showNotific(name, savedPath + "/" + name);
//                    context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(savedPath + "/" + name))));
                    return null;
                } catch (Exception unused) {
                    Log.d("Exception", "doInBackground: "+unused);
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public void showNotific(String title, String content) {
        if (title.contains("IMG") || title.contains("VID")) {
            String name = "";
            if (title.contains("IMG")) {
                name = context.getString(R.string.a_deleted_picture_was_recovered);
            } else if (title.contains("VID")) {
                name = context.getString(R.string.a_deleted_video_was_recovered);
            }

            String finalName = name;
            Glide.with(context)
                    .asBitmap()
                    .load(content)
                    .into(new CustomTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            showNotificationBigStyle(context, new Random()
                                            .nextInt(), context.getString(R.string.message_recovery),
                                    finalName,
                                    resource,
                                    null);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } else  {

            String name = "";
            if (title.contains("AUD")) {
                name = context.getString(R.string.a_deleted_audio_was_recovered);
            } else if (title.contains("apk") || title.contains("ppt")
                    || title.contains("pdf") || title.contains("zip") || title.contains("rar")
                    || title.contains("7zip") || title.contains("xls") || title.contains("docx")) {
                name = context.getString(R.string.a_deleted_document_was_recovered);
            }
            showNotification(context, new Random().nextInt(), context.getString(R.string.message_recovery), name, null);
        }
    }

    public void showNotificationBigStyle(Context context, int id, String title, String content, Bitmap bitmap, Intent intent) {
        PendingIntent pendingIntent = null;
        if (pendingIntent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE);
        String NOTIFICATION_CHANNEL_ID = "WhatsappRecovery";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.whatsApp_recovery), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.whatsApp_recovery));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);

    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (pendingIntent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE);
        String NOTIFICATION_CHANNEL_ID = "WA_Recovery";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.wa_recovery), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.wa_recovery));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);

    }
}
