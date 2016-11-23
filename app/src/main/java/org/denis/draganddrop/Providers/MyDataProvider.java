package org.denis.draganddrop.Providers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import org.denis.draganddrop.Model.MyContentProvider;
import org.denis.draganddrop.Model.SQLiteHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ustad on 21.11.2016.
 */
public class MyDataProvider extends AbstractDataProvider {
    private List<CurrentData> mData;

    public MyDataProvider(ContentResolver contentResolver, boolean bUseDB) {
        mData = new LinkedList<>();
        Cursor cursor;

        if(bUseDB) {
            cursor = contentResolver.query(MyContentProvider.CONTENT_URI, null, null, null, null);

            if (cursor.getCount() > 0) {
                Map<Integer, CurrentData> tmpMap = new HashMap<>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.CUR_ID));
                    int prevId = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PREV_ID));
                    int nextId = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.NEXT_ID));
                    String name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.NAME));

                    String itemText = "ID: " + id + " Name: " + name;

                    int viewType = 0;
                    int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
                    tmpMap.put(prevId, new CurrentData(id, prevId, nextId, viewType, itemText, swipeReaction));
                }

                int index = -1;
                while (true){
                    CurrentData data = tmpMap.get(index);
                    mData.add(data);
                    index = (int) data.getId();

                    if(data.getNextId() == -1) break;
                }
            }
        } else {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            int currentId = 0;
            int count = cursor.getCount();

            if (count > 0) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    ContentValues values = new ContentValues();
                    values.put(SQLiteHelper.CUR_ID, currentId);
                    values.put(SQLiteHelper.NAME, name);
                    if((currentId + 1) >= count){
                        values.put(SQLiteHelper.NEXT_ID, "-1");
                    } else {
                        values.put(SQLiteHelper.NEXT_ID, (currentId + 1) + "");
                    }
                    values.put(SQLiteHelper.PREV_ID, (currentId - 1) + "");

                    contentResolver.insert(MyContentProvider.BASE_URI, values);

                    String itemText = "ID: " + currentId + " Name: " + name;

                    int viewType = 0;
                    int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
                    mData.add(new CurrentData(currentId, currentId - 1, currentId + 1, viewType, itemText, swipeReaction));

                    currentId++;
                }
            }
        }

        cursor.close();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        // Need not
        return 0;
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final CurrentData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mData, toPosition, fromPosition);
    }

    @Override
    public void removeItem(int position) {
        //Need not
    }

    public static final class CurrentData extends Data {

        private final long mId;
        private final long mPrevId;
        private final long mNextId;
        private final String mText;
        private final int mViewType;
        private boolean mPinned;

        CurrentData(long id, long prevId,long nextId, int viewType, String text, int swipeReaction) {
            mId = id;
            mPrevId = prevId;
            mNextId = nextId;

            mViewType = viewType;
            mText = makeText(id, text, swipeReaction);
        }

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();

            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return sb.toString();
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        @Override
        public long getPrevId() {
            return mPrevId;
        }

        @Override
        public long getNextId() {
            return mNextId;
        }

        @Override
        public String toString() {
            return mText;
        }

        @Override
        public String getText() {
            return mText;
        }

        @Override
        public boolean isPinned() {
            return mPinned;
        }

        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }
    }
}
