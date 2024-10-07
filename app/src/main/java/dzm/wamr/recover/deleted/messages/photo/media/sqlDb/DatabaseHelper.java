package dzm.wamr.recover.deleted.messages.photo.media.sqlDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import dzm.wamr.recover.deleted.messages.photo.media.model.DataModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    //    public LiveData<ArrayList<DataModel>> arrayListLiveData;
    Context context;
    MutableLiveData<ArrayList<DataModel>> resultList;
    ArrayList<DataModel> myList;
    List<String> list;

    public DatabaseHelper(Context context) {
        super(context, "Userdata.db", null, 3);
        this.context = context;
    }

//    public void initializeArray(){
//        arrayListLiveData =  new  MutableLiveData<ArrayList<DataModel>>();
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create Table Userdetails( id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " + "msg TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL," + " licon TEXT NOT NULL, type TEXT NOT NULL, grpName TEXT NOT NULL)");
        db.execSQL("create Table chat( id INTEGER PRIMARY KEY AUTOINCREMENT, fileName TEXT NOT NULL, filePath TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, licon TEXT NOT NULL)");

    }

    public Boolean insertFileData(String fileName, String filePath, String date, String time, String licon) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("fileName", fileName);
        contentValues.put("filePath", filePath);
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("licon", licon);

        // contentValues.put("dob", bitmap);
        long result = DB.insertWithOnConflict("chat", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        DB.close();
        if (result == -1) {
            return false;

        } else {

//            if (context !=null && context instanceof MainActivity){
//                ((MainActivity)context).updateDataCheck();
//
//            }
//            try{
//                MainActivity activity =new MainActivity();
//                activity.updateDataCheck();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            Log.d("Loggg", "valueee");
            return true;

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("drop Table if exists Userdetails");
        db.execSQL("drop Table if exists chat");
        db.execSQL("create Table Userdetails( id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " + "msg TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL," + " licon TEXT NOT NULL, type TEXT NOT NULL, grpName TEXT NOT NULL)");
        db.execSQL("create Table chat( id INTEGER PRIMARY KEY AUTOINCREMENT, fileName TEXT NOT NULL, filePath TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, licon TEXT NOT NULL)");

    }

    /*public Boolean insertMessages(String name, String msg, String date, String time, String licon) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //  contentValues.put("globeid", globeid);
        contentValues.put("name", name);
        contentValues.put("msg", msg);
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("licon", licon);
        // contentValues.put("dob", bitmap);
        long result = DB.insert("chat", null, contentValues);
        if (result == -1) {
            return false;

        } else {
            Log.d("Loggg", "valueee");
            return true;

        }
    }*/


    public Boolean insertuserdata(String name, String msg, String date, String time, String licon, String type, String grp) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("msg", msg);
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("licon", licon);
        contentValues.put("type", type);
        contentValues.put("grpName", grp);
        // contentValues.put("dob", bitmap);
        long result = DB.insertWithOnConflict("Userdetails", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        DB.close();
        if (result == -1) {
            return false;

        } else {
            return true;

        }

    }

    public Cursor getData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("Select * from Userdetails", null);
    }

    public Cursor getSpecificData(String name) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            return db.rawQuery("Select * from Userdetails where name = ?  ", new String[]{name});
//            return db.rawQuery("Select * From Userdetails ", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    // below is the method for deleting our course.
    public void deleteSpecificData(String name) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("Userdetails", "name=?", new String[]{name});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<DataModel> getdata() {
        try {
            SQLiteDatabase DB = getWritableDatabase();
            Cursor cursor = DB.rawQuery("Select * from Userdetails order by id DESC", null);
            myList = new ArrayList<>();
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String Msg = cursor.getString(2);
                String date = cursor.getString(3);
                String time = cursor.getString(4);
                String image = cursor.getString(5);
                String type = cursor.getString(6);
                String mbrName = cursor.getString(7);
                if (!list.contains(name)) {
                    list.add(name);
                    myList.add(new DataModel(id, name, Msg, date, time, image, type, mbrName));
                }
            }
            Log.d("List", list.toString());
            DB.close();
            cursor.close();
            return myList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface DataInserted {
        public void onDataInserted();
    }

}
