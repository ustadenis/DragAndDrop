package org.denis.draganddrop.Presenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import org.denis.draganddrop.MainActivity;
import org.denis.draganddrop.Model.MyContentProvider;
import org.denis.draganddrop.Model.SQLiteHelper;
import org.denis.draganddrop.MyDraggableItemAdapter;
import org.denis.draganddrop.Providers.MyDataProvider;

/**
 * Created by ustad on 21.11.2016.
 */
public class DataPresenter implements DataCallback {
    private MainActivity mView;
    private ContentResolver mContentResolver;
    private MyDataProvider mDataProvider;
    private RecyclerView.Adapter mAdapter;
    private boolean mIsFirstStart;

    public DataPresenter(MainActivity view, boolean bIsFirstStart) {
        mView = view;
        mContentResolver = mView.getContentResolver();
        mIsFirstStart = bIsFirstStart;

        PrepareDataAsyncTask prepareDataAsyncTask = new PrepareDataAsyncTask(this);
        prepareDataAsyncTask.execute();
    }

    @Override
    public void onDataMoved(int fromId, int toId, int fromPos, int toPos) {
        UpdateDataAsyncTask updateDataAsyncTask = new UpdateDataAsyncTask();
        updateDataAsyncTask.execute(new Object[] { fromId, toId, fromPos, toPos });
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    public MyDataProvider getDataProvider() {
        return mDataProvider;
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    class UpdateDataAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            int fromId = (int) params[0];
            int toId = (int) params[1];

            int fromPos = (int) params[2];
            int toPos = (int) params[3];

            Cursor fromCursor = mContentResolver.query(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromId + ""), null, null, null, null);
            Cursor toCursor = mContentResolver.query(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, toId + ""), null, null, null, null);

            if(fromCursor.getCount() > 0 && toCursor.getCount() > 0) {

                fromCursor.moveToNext();
                toCursor.moveToNext();

                int fromPrevId = fromCursor.getInt(fromCursor.getColumnIndex(SQLiteHelper.PREV_ID));
                int fromNextId = fromCursor.getInt(fromCursor.getColumnIndex(SQLiteHelper.NEXT_ID));
                int toPrevId = toCursor.getInt(toCursor.getColumnIndex(SQLiteHelper.PREV_ID));
                int toNextId = toCursor.getInt(toCursor.getColumnIndex(SQLiteHelper.NEXT_ID));

                /*From logic*/
                if(fromPrevId >= 0 && fromNextId >= 0) {
                    ContentValues fromValues1 = new ContentValues();
                    fromValues1.put(SQLiteHelper.NEXT_ID, fromNextId);

                    ContentValues fromValues2 = new ContentValues();
                    fromValues2.put(SQLiteHelper.PREV_ID, fromPrevId);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromPrevId + ""), fromValues1, null, null);
                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromNextId + ""), fromValues2, null, null);
                }

                if(fromPrevId == -1) {
                    ContentValues fromValues2 = new ContentValues();
                    fromValues2.put(SQLiteHelper.PREV_ID, -1);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromNextId + ""), fromValues2, null, null);
                }

                if(fromNextId == -1) {
                    ContentValues fromValues1 = new ContentValues();
                    fromValues1.put(SQLiteHelper.NEXT_ID, -1);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromPrevId + ""), fromValues1, null, null);
                }
                /*End from logic*/

                /*To logic*/
                if(fromPos < toPos) {
                    ContentValues toValues = new ContentValues();
                    toValues.put(SQLiteHelper.NEXT_ID, fromId);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, toId + ""), toValues, null, null);

                    ContentValues toValues1 = new ContentValues();
                    toValues1.put(SQLiteHelper.PREV_ID, toId);
                    toValues1.put(SQLiteHelper.NEXT_ID, toNextId);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromId + ""), toValues1, null, null);

                    if(toNextId >= 0) {
                        ContentValues toValues2 = new ContentValues();
                        toValues2.put(SQLiteHelper.PREV_ID, fromId);

                        mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, toNextId + ""), toValues2, null, null);
                    }
                } else {
                    ContentValues toValues = new ContentValues();
                    toValues.put(SQLiteHelper.PREV_ID, fromId);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, toId + ""), toValues, null, null);

                    ContentValues toValues1 = new ContentValues();
                    toValues1.put(SQLiteHelper.PREV_ID, toPrevId);
                    toValues1.put(SQLiteHelper.NEXT_ID, toId);

                    mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, fromId + ""), toValues1, null, null);

                    if(toPrevId >= 0) {
                        ContentValues toValues2 = new ContentValues();
                        toValues2.put(SQLiteHelper.NEXT_ID, fromId);

                        mContentResolver.update(Uri.withAppendedPath(MyContentProvider.CONTENT_URI, toPrevId + ""), toValues2, null, null);
                    }
                }
                /*End to logic*/
            }

            fromCursor.close();
            toCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.onDataUpdated();
                    }
                });
        }
    }

    class PrepareDataAsyncTask extends AsyncTask {

        private DataPresenter mPresenter;

        public PrepareDataAsyncTask(DataPresenter presenter) {
            mPresenter = presenter;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            mDataProvider = new MyDataProvider(mContentResolver, mIsFirstStart);
            mAdapter = new MyDraggableItemAdapter(mDataProvider, mPresenter);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mAdapter != null) {
                        mView.onDataReady(mAdapter);
                    }
                }
            });
        }
    }
}
