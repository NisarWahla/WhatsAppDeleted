package dzm.wamr.recover.deleted.messages.photo.media.FileObserve;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import dzm.wamr.recover.deleted.messages.photo.media.model.Status;
import dzm.wamr.recover.deleted.messages.photo.media.util.Const;

public class Fuction {
    public static ArrayList<Status> getDataForAndroid11OnlyStatus(Uri uriMain, Context context) {
        ArrayList<Status> arrayList = new ArrayList();
        //check for app specific folder permission granted or not
        Log.d("====", "uriMain ::: " + uriMain);
        ContentResolver contentResolver = context.getContentResolver();
        Uri buildChildDocumentsUriUsingTree = DocumentsContract.buildChildDocumentsUriUsingTree(uriMain, DocumentsContract.getDocumentId(uriMain));
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(buildChildDocumentsUriUsingTree, new String[]{"document_id"}, (String) null, (String[]) null, (String) null);
            while (cursor.moveToNext()) {
                //arrayList.add(DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)));
                if (!DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)).toString().endsWith(".nomedia")) {
                   String name = new File(DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)).getPath()).getName();
                    String fileEnd = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
                    boolean isVideo;
                    if (fileEnd.contains("mp4")){
                        isVideo = true;
                    } else {
                        isVideo = false;
                    }
                    Status status = new Status(new File(DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)).getPath()), new File(DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)).getPath()).getName(), DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)).getPath(), isVideo, DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)));
                    //arrayList.add(DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)));
                    arrayList.add(status);
                    Log.d(Const.tag, "" + DocumentsContract.buildDocumentUriUsingTree(uriMain, cursor.getString(0)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static void deletFile(String str, Context context) {
        String str2;
        File file = new File(str);
        ContentResolver contentResolver = context.getContentResolver();
        try {
            str2 = file.getCanonicalPath();
        } catch (IOException unused) {
            str2 = file.getAbsolutePath();
        }
        Uri contentUri = MediaStore.Files.getContentUri("external");
        if (contentResolver.delete(contentUri, "_data=?", new String[]{str2}) == 0) {
            String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(str2)) {
                contentResolver.delete(contentUri, "_data=?", new String[]{absolutePath});
            }
        }
        if (file.exists()) {
            file.delete();
            if (file.exists()) {
                try {
                    file.getCanonicalFile().delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (file.exists()) {
                    context.deleteFile(file.getName());
                }
            }
        }
    }

    public static void deleteFiles(File file, Context context) {
        try {
            File[] listFiles = file.listFiles();
            int length = listFiles.length;
            for (int i = 0; i < length; i++) {
                File file2 = listFiles[i];
                if (file2.isFile()) {
                    ContentResolver contentResolver = context.getContentResolver();
                    String str = file2.getCanonicalPath();
                    Uri contentUri = MediaStore.Files.getContentUri("external");
                    if (contentResolver.delete(contentUri, "_data=?", new String[]{str}) == 0) {
                        String absolutePath = file2.getAbsolutePath();
                        if (!absolutePath.equals(str)) {
                            contentResolver.delete(contentUri, "_data=?", new String[]{absolutePath});
                        }
                    }
                    if (file2.exists()) {
                        file2.delete();
                        if (file2.exists()) {
                            try {
                                file2.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (file2.exists()) {
                                context.deleteFile(file2.getName());
                            }
                        }
                    }
                } else {
                    deleteFiles(file2, context);
                }
            }
        } catch (Exception unused) {
        }
    }

    public static void deleteFiles_one(File file, Context context) {
        try {
            File[] listFiles = file.listFiles();
            int length = listFiles.length;
            for (int i = 0; i < length; i++) {
                File file2 = listFiles[i];
                if (file2.isFile()) {
                    ContentResolver contentResolver = context.getContentResolver();
                    String str = file2.getCanonicalPath();
                    Uri contentUri = MediaStore.Files.getContentUri("external");
                    if (contentResolver.delete(contentUri, "_data=?", new String[]{str}) == 0) {
                        String absolutePath = file2.getAbsolutePath();
                        if (!absolutePath.equals(str)) {
                            contentResolver.delete(contentUri, "_data=?", new String[]{absolutePath});
                        }
                    }
                }
                if (file2.exists()) {
                    file2.delete();
                    if (file2.exists()) {
                        try {
                            file2.getCanonicalFile().delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (file2.exists()) {
                            context.deleteFile(file2.getName());
                        }
                    }
                }
            }
        } catch (Exception unused) {
        }
    }

    public static List<File> getListFiles_one(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            try {
                Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                return Arrays.asList(listFiles);
            } catch (Exception unused) {
            }
        }
        return null;
    }

}

