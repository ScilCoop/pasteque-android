package fr.pasteque.client.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import fr.pasteque.client.utils.PdfCreator;

/**
 * Created by svirch_n on 26/04/16
 * Last edited at 10:02.
 */
public class Pdf extends LinearLayout {
    public Pdf(Context context) {
        super(context);
    }

    public Pdf(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Pdf(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Pdf(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(PdfCreator.PDF_WIDTH_DP, PdfCreator.PDF_HEIGHT_DP);
    }
}
