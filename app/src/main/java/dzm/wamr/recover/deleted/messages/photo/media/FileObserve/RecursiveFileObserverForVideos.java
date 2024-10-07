package dzm.wamr.recover.deleted.messages.photo.media.FileObserve;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.FileObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import dzm.wamr.recover.deleted.messages.photo.media.sqlDb.DatabaseHelper;
import dzm.wamr.recover.deleted.messages.photo.media.util.Common;

public class RecursiveFileObserverForVideos extends FileObserver {
    public static int CHANGES_ONLY = 3016;
    public static FileObserver observer;
    SharedPreferences a;
    SharedPreferences b;
    SharedPreferences c;
    DatabaseHelper databaseHelper;
    List<SingleFileObserverForVideos> singleFileObservers;
    String string;
    int f;
    Context context;

    class SingleFileObserverForVideos extends FileObserver {
        String a;

        public SingleFileObserverForVideos(RecursiveFileObserverForVideos recursiveFileObserver, String str) {
            this(str, 4095);
            this.a = str;
        }

        public SingleFileObserverForVideos(String str, int i) {
            super(str, i);
            this.a = str;
        }

        public void onEvent(int i, String str) {
            RecursiveFileObserverForVideos.this.onEvent(i, this.a  + str);
        }
    }

    public RecursiveFileObserverForVideos(String str, int i) {
        super(str, i);
        this.string = str;
        this.f = i;
    }

    public RecursiveFileObserverForVideos(String str, Context context) {
        this(str, FileObserver.ALL_EVENTS);
        this.context = context;
    }


    public void onEvent(int i, String str) {
        switch (i) {

            case 512:

                Intent intent= new Intent(Common.ACTION_DELETE);
                intent.putExtra(Common.ACTION_TYPE,Common.ACTION_VIDEO_DELETE);
                intent.putExtra(Common.ACTION_PATH,str);
                context.sendBroadcast(intent);
                break;

            default:
                return;
        }
    }

    public void startWatching() {
       try {

           if (this.singleFileObservers == null) {
               this.singleFileObservers = new ArrayList();
               Stack stack = new Stack();
               stack.push(this.string);
               while (!stack.isEmpty()) {
                   String str = (String) stack.pop();
                   this.singleFileObservers.add(new SingleFileObserverForVideos(str, this.f));


                   File[] listFiles = new File(str).listFiles();
                   if (listFiles != null) {
                       for (File file : listFiles) {

                           if (!file.isDirectory() ) {
                               stack.push(file.getPath());

//                            databaseHelper.insertFileData(file.getName(), file.getPath(),
//                                    currentDate.toString(), currentTime.toString() + " " + dayOfTheWeek, " ");

                           }
                       }
                   }
               }
               for (SingleFileObserverForVideos startWatching : this.singleFileObservers) {
                   startWatching.startWatching();
               }
           }
       }catch (Exception ignored){

       }
    }

    public void stopWatching() {
        if (this.singleFileObservers != null) {
            for (SingleFileObserverForVideos stopWatching : this.singleFileObservers) {
                stopWatching.stopWatching();
            }
            this.singleFileObservers.clear();
            this.singleFileObservers = null;
        }
    }
}