package fr.pasteque.client.widgets.pdf;

import android.content.Context;
import android.widget.TableLayout;
import fr.pasteque.client.models.Ticket;

import java.text.DecimalFormat;

/**
 * Created by svirch_n on 28/04/16
 * Last edited at 16:32.
 */
public class PdfTotalRow extends PdfRow {


    public PdfTotalRow(Context applicationContext, TableLayout content) {
        super(applicationContext, content);
    }


    public void setTotal(Ticket total) {
        DecimalFormat df = new DecimalFormat("#.00");
        String[] elements = {
                null,
                String.valueOf(df.format(round(total.getTicketPriceExcTax()))),
                String.valueOf(df.format(round(total.getTicketTax()))),
                String.valueOf(df.format(round(total.getTicketPrice())))
        };
        addElements(elements);
    }

    public void setHeader() {
        String[] elements = {
                "Total ",
                "HT",
                "Taxe",
                "TTC",
        };
        addElements(elements);
    }
}
