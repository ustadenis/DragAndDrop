package org.denis.draganddrop.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ustad on 21.11.2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "contacts_database";
    public final static String DATABASE_TABLE = "contacts_table";
    public final static int DATABASE_VERSION = 1;

    public final static String KEY_ID = "_id";
    public final static String NAME = "name";
    public final static String CUR_ID = "curid";
    public final static String PREV_ID = "prev";
    public final static String NEXT_ID = "next";

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                DATABASE_TABLE + " (" +
                KEY_ID + " integer primary key autoincrement, " +
                NAME + " text, " +
                CUR_ID + " integer, " +
                PREV_ID + " integer, " +
                NEXT_ID + " integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist " +
                DATABASE_TABLE);
        onCreate(db);
    }
}
