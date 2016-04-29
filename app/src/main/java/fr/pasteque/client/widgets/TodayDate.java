package fr.pasteque.client.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.utils.Date;
import fr.pasteque.client.widgets.time.DateView;
import fr.pasteque.client.widgets.time.TimeView;

/**
 * Created by svirch_n on 29/04/16
 * Last edited at 10:39.
 */
public class TodayDate extends LinearLayout {
    public TodayDate(Context context) {
        super(context);
        this.init();
    }

    public TodayDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public TodayDate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TodayDate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    public void init() {
        setOrientation(VERTICAL);
        Date date = Pasteque.now();
        new TimeView(this, "Time: ", date);
        new DateView(this, "Date: ", date);

    }
}
