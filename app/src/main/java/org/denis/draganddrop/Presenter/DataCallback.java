package org.denis.draganddrop.Presenter;

/**
 * Created by ustad on 21.11.2016.
 */
public interface DataCallback {
    void onDataMoved(int fromId, int toId, int fromPos, int toPos);
    void onStart();
    void onStop();
}
