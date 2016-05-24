package fr.pasteque.client.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by svirch_n on 24/05/16
 * Last edited at 10:29.
 */
public class RestaurantLayout extends FrameLayout {
    public RestaurantLayout(Context context) {
        super(context);
    }

    public RestaurantLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RestaurantLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RestaurantLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).layout(left, top, right, bottom);
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }
}
