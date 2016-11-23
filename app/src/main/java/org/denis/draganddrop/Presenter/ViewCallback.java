package org.denis.draganddrop.Presenter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by ustad on 22.11.2016.
 */
public interface ViewCallback {
    void onDataReady(RecyclerView.Adapter adapter);
    void onDataUpdated();
}
