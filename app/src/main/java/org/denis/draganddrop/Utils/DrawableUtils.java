package org.denis.draganddrop;

import android.graphics.drawable.Drawable;

/**
 * Created by ustad on 21.11.2016.
 */
public class DrawableUtils {
    private static final int[] EMPTY_STATE = new int[] {};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }
}
