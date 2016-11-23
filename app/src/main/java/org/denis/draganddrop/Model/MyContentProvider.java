package org.denis.draganddrop.Model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by ustad on 21.11.2016.
 */
public class MyContentProvider extends ContentProvider {
    private final static String TAG = MyContentProvider.class.getName();

    public final static String AUTHORITY = "org.denis.draganddrop.Model.MyContentProvider";
    public final static Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, SQLiteHelper.DATABASE_TABLE);

    private SQLiteHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;
    private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CONTACTS_ALL = 1;
    private static final int CONTACT_ID = 2;

    static {
        mUriMatcher.addURI(AUTHORITY, SQLiteHelper.DATABASE_TABLE, CONTACTS_ALL);
        mUriMatcher.addURI(AUTHORITY, SQLiteHelper.DATABASE_TABLE + "/#", CONTACT_ID);
    }

    public void openDatabase() {
        Context context = getContext();
        try {
            if(mDatabaseHelper == null) {
                mDatabaseHelper = new SQLiteHelper(context, SQLiteHelper.DATABASE_NAME, null, SQLiteHelper.DATABASE_VERSION);
            }
            if(mDatabase == null) {
                mDatabase = mDatabaseHelper.getWritableDatabase();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            mDatabase = mDatabaseHelper.getReadableDatabase();
        }
    }

    @Override
    public boolean onCreate() {
        openDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SQLiteHelper.DATABASE_TABLE);

        switch (mUriMatcher.match(uri)) {
            case CONTACT_ID: {
                String id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(SQLiteHelper.CUR_ID + "=" + id);
                break;
            }
            case CONTACTS_ALL: {
                break;
            }
        }

        Cursor cursor = queryBuilder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case CONTACT_ID: {
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + SQLiteHelper.DATABASE_TABLE;
            }
            case CONTACTS_ALL: {
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + SQLiteHelper.DATABASE_TABLE;
            }
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = mDatabase.insert(SQLiteHelper.DATABASE_TABLE, null, values);

        if(id >= 0) {
            Uri insertedId = ContentUris.withAppendedId(BASE_URI, id);
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            return insertedId;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(selection == null) {
            selection = "1";
        }
        int deleteCount = mDatabase.delete(SQLiteHelper.DATABASE_TABLE, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (mUriMatcher.match(uri)) {
            case CONTACT_ID: {
                String id = uri.getPathSegments().get(1);
                selection = SQLiteHelper.CUR_ID + "=" + id +
                        (!TextUtils.isEmpty(selection) ? (" AND (" + selection + ")") : "");
                break;
            }
        }

        int updatedCount = mDatabase.update(SQLiteHelper.DATABASE_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updatedCount;
    }
}
