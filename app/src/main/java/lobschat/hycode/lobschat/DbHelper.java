package lobschat.hycode.lobschat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class DbHelper extends SQLiteOpenHelper {

    String OfflineChat="OfflineChat";

    String localDB = "lobschatdb.db";

    public DbHelper(Context context) {
//      int  version=1;
//       String name="lobschatdb.db";
//       SQLiteDatabase.CursorFactory factory=null;
        super(context, "lobschatdb.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String offlieChat = "CREATE TABLE OfflineChat ( " +
                "Id INTEGER," +
                "UniqueId TEXT," +
                "msgText TEXT,"+
                "chatWithId TEXT,"+
                "chatWith Text)";

                db.execSQL(offlieChat);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String offlineChat = "DROP TABLE IF EXISTS " + OfflineChat;
        db.execSQL(offlineChat);
    }
    public void deleteByUniqueId(String UniqueId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(OfflineChat, "UniqueId=?", new String[]{UniqueId});
    }


    public void insertOfflineChat(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("UniqueId", queryValues.get("UniqueId"));
        values.put("msgText", queryValues.get("msgText"));
        values.put("chatWith", queryValues.get("chatWith"));
        values.put("chatWithId", queryValues.get("chatWithId"));

        database.insert(OfflineChat, null, values);
        database.close();
    }


    public ArrayList<HashMap<String,String>> getAllOfflineChat() {
        ArrayList<HashMap<String, String>> result;
        result = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT  * FROM " + OfflineChat;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> values = new HashMap<String, String>();

                values.put("msgText", cursor.getString(cursor.getColumnIndex("msgText")));
                values.put("UniqueId", cursor.getString(cursor.getColumnIndex("UniqueId")));
                values.put("chatWith", cursor.getString(cursor.getColumnIndex("chatWith")));
                values.put("chatWithId", cursor.getString(cursor.getColumnIndex("chatWithId")));
                result.add(values);
            } while (cursor.moveToNext());
        }
        database.close();
        return result;
//        Gson gson = new GsonBuilder().create();
//        //Use GSON to serialize Array List to JSON
//        return gson.toJson(wordList);
    }



    public ArrayList<HashMap<String,String>> getOfflineChatByUniqueId(String UniqueId) {
        ArrayList<HashMap<String, String>> resut;
        resut = new ArrayList<HashMap<String, String>>();
        String[] selection = new String[]{UniqueId};
        String selectQuery = "SELECT  * FROM " + OfflineChat + " Where UniqueId = ? ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, selection);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> values = new HashMap<String, String>();
                values.put("msgText", cursor.getString(cursor.getColumnIndex("msgText")));
                values.put("UniqueId", cursor.getString(cursor.getColumnIndex("UniqueId")));
                values.put("chatWith", cursor.getString(cursor.getColumnIndex("chatWith")));
                values.put("chatWithId", cursor.getString(cursor.getColumnIndex("chatWithId")));

                resut.add(values);
            } while (cursor.moveToNext());
        }
        database.close();
        return resut;
//        Gson gson = new GsonBuilder().create();
//        //Use GSON to serialize Array List to JSON
//        return gson.toJson(wordList);
    }
    public ArrayList<HashMap<String,String>> getOfflineChatByChatWith(String chatWith) {
        ArrayList<HashMap<String, String>> result;
        result = new ArrayList<HashMap<String, String>>();
        String[] selection = new String[]{chatWith};
        String selectQuery = "SELECT  * FROM " + OfflineChat + " Where chatWith = ? ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, selection);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> values = new HashMap<String, String>();
                values.put("msgText", cursor.getString(cursor.getColumnIndex("msgText")));
                values.put("UniqueId", cursor.getString(cursor.getColumnIndex("UniqueId")));
                values.put("chatWith", cursor.getString(cursor.getColumnIndex("chatWith")));
                values.put("chatWithId", cursor.getString(cursor.getColumnIndex("chatWithId")));

                result.add(values);
            } while (cursor.moveToNext());
        }
        database.close();
        return result;
//        Gson gson = new GsonBuilder().create();
//        //Use GSON to serialize Array List to JSON
//        return gson.toJson(wordList);
    }

    public ArrayList<HashMap<String,String>> getOfflineChatByChatWithId(String chatWithId) {
        ArrayList<HashMap<String, String>> result;
        result = new ArrayList<HashMap<String, String>>();
        String[] selection = new String[]{chatWithId};
        String selectQuery = "SELECT  * FROM " + OfflineChat + " Where chatWithId = ? ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, selection);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> values = new HashMap<String, String>();
                values.put("msgText", cursor.getString(cursor.getColumnIndex("msgText")));
                values.put("UniqueId", cursor.getString(cursor.getColumnIndex("UniqueId")));
                values.put("chatWith", cursor.getString(cursor.getColumnIndex("chatWith")));
                values.put("chatWithId", cursor.getString(cursor.getColumnIndex("chatWithId")));

                result.add(values);
            } while (cursor.moveToNext());
        }
        database.close();
        return result;
//        Gson gson = new GsonBuilder().create();
//        //Use GSON to serialize Array List to JSON
//        return gson.toJson(wordList);
    }


}
