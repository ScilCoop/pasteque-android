package fr.pasteque.client.widgets.pdf;

import android.content.Context;
import android.widget.TableLayout;
import fr.pasteque.client.utils.CalculPrice;

import java.text.DecimalFormat;

/**
 * Created by svirch_n on 28/04/16
 * Last edited at 15:56.
 */
public class PdfTaxeRow extends PdfRow {

    public PdfTaxeRow(Context context, TableLayout root) {
        super(context, root);
    }

    public void setHeader() {
        String[] elements = {
                "TVA ",
                null,
                null,
                null,
                "HT",
                "Taxe",
        };
        addElements(elements);
    }

    public void setTaxe(Double taxe, Double taxeValue, Double excValue) {
        String[] elements = {
                formatTax(taxe),
                null,
                null,
                null,
                String.valueOf(round(excValue)),
                String.valueOf(round(taxeValue))
        };
        addElements(elements);
    }
}
