package dzm.wamr.recover.deleted.messages.photo.media.FileObserve;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import dzm.wamr.recover.deleted.messages.photo.media.util.Common;

public class RecursiveFileObserverForDoc extends FileObserver {
    public static int CHANGES_ONLY = 3016;
    public static FileObserver observer;
    SharedPreferences a;
    SharedPreferences b;
    SharedPreferences c;

    List<SingleFileObserverForDoc> singleFileObservers;
    String string;
    int f;
    Context context;

    class SingleFileObserverForDoc extends FileObserver {
        String a;

        public SingleFileObserverForDoc(String str, int i) {
            super(str, i);
            this.a = str;
        }

        public void onEvent(int i, String str) {
            RecursiveFileObserverForDoc.this.onEvent(i, this.a  + str);
        }
    }

    public RecursiveFileObserverForDoc(String str, int i) {
        super(str, i);
        this.string = str;
        this.f = i;
    }

    public RecursiveFileObserverForDoc(String str, Context context) {
        this(str, FileObserver.ALL_EVENTS);
        this.context = context;
    }


    public void onEvent(int i, String str) {
//        Log.d("Deleted", "onEvent: " + str);
        if (i == 512) {
            Log.d("Deleted", "onEvent: " + str);
            Intent intent = new Intent(Common.ACTION_DELETE);
            intent.putExtra(Common.ACTION_TYPE, Common.ACTION_IMAGE_DELETE);
            intent.putExtra(Common.ACTION_PATH, str);
            context.sendBroadcast(intent);
        } else {
            return;
        }
    }

    public void startWatching() {


        if (this.singleFileObservers == null) {
            this.singleFileObservers = new ArrayList();
            Stack stack = new Stack();
            stack.push(this.string);
            while (!stack.isEmpty()) {
                String str = (String) stack.pop();
                this.singleFileObservers.add(new SingleFileObserverForDoc(str, this.f));

                Log.d("observerDele", "onEvent: " + str);
                File[] listFiles = new File(str).listFiles();
                if (listFiles != null) {
                    for (File file : listFiles) {

                        if (!file.isDirectory() ) {
                            stack.push(file.getPath());

                        }
                    }
                }
            }
            for (SingleFileObserverForDoc startWatching : this.singleFileObservers) {
                startWatching.startWatching();
            }
        }
    }

    public void stopWatching() {
        if (this.singleFileObservers != null) {
            for (SingleFileObserverForDoc stopWatching : this.singleFileObservers) {
                stopWatching.stopWatching();
            }
            this.singleFileObservers.clear();
            this.singleFileObservers = null;
        }
    }
}