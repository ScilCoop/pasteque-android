package fr.pasteque.client.widgets.time;

import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.pasteque.client.utils.Date;
import fr.pasteque.client.widgets.TodayDate;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by svirch_n on 29/04/16
 * Last edited at 11:23.
 */
public abstract class DateTimeView extends TextView {

    private String preText;
    private Date date;

    public DateTimeView(TodayDate todayDate, String preText, Date date) {
        super(todayDate.getContext());
        this.preText = preText;
        this.date = date;
        this.init(todayDate);
    }

    private void init(TodayDate root) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        setLayoutParams(params);
        root.addView(this);
        String text = preText + getTimeFormat(this.date);
        setText(text);
    }

    protected abstract String getTimeFormat(Date date);
}
