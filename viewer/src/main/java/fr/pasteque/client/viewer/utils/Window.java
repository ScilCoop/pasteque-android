package fr.pasteque.client.viewer.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by svirch_n on 18/04/16
 * Last edited at 11:35.
 */
public class Window {

    private static int width;
    private static int height;

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static void calcWindow(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels - calcActionBar(activity.getApplicationContext().getTheme(), activity.getResources());
        width = displaymetrics.widthPixels;
    }

    private static int calcActionBar(Resources.Theme theme, Resources resources) {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }
        return 0;
    }
}
