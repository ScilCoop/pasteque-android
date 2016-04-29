package fr.pasteque.client.widgets.pdf;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import fr.pasteque.client.R;
import fr.pasteque.client.utils.CalculPrice;

import java.text.DecimalFormat;

/**
 * Created by svirch_n on 27/04/16
 * Last edited at 10:04.
 */
public abstract class PdfRow extends TableRow {

    static final int PADDING = 0;
    static final int CHILD_PADDING = 10;
    static final int ROUND = 2;
    private int[] gravity =   {
            Gravity.LEFT,
            Gravity.RIGHT,
            Gravity.RIGHT,
            Gravity.RIGHT,
            Gravity.RIGHT,
            Gravity.RIGHT
    };


    public PdfRow(Context context, TableLayout root) {
        super(context);
        init(root);
    }

    public PdfRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PdfRow(Context context) {
        super(context);
    }

    private void init(TableLayout root) {
        if (root != null) {
            TableLayout.LayoutParams rootParams = root.generateLayoutParams(null);
            rootParams.setMargins(0, PADDING, 0, PADDING);
            setLayoutParams(rootParams);
            root.addView(this);
        }
    }

    protected void addElements(String[] elements) {

        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                TextView textView = (TextView) inflate(getContext(), R.layout.pdf_text_line, null);
                LayoutParams params = (LayoutParams) this.generateDefaultLayoutParams();
                params.setMargins(CHILD_PADDING, CHILD_PADDING, CHILD_PADDING, CHILD_PADDING);
                params.column = i;
                textView.setLayoutParams(params);
                textView.setText(elements[i]);
                textView.setGravity(gravity[i]);
                this.addView(textView);
            }
        }
    }

    protected double round(Double number) {
        return CalculPrice.round(number, ROUND);
    }

    protected String formatTax(Double taxe) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(taxe * 100) + "%";
    }
}
