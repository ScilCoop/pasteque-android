package fr.pasteque.client.widgets.time;

import fr.pasteque.client.utils.Date;
import fr.pasteque.client.widgets.TodayDate;

/**
 * Created by svirch_n on 29/04/16
 * Last edited at 11:28.
 */
public class TimeView extends DateTimeView {

    public TimeView(TodayDate todayDate, String preText, Date date) {
        super(todayDate, preText, date);
    }

    @Override
    protected String getTimeFormat(Date date) {
        return date.formatTime();
    }
}
