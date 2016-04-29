package fr.pasteque.client.widgets.pdf;

import android.content.Context;
import android.widget.TableLayout;
import fr.pasteque.client.models.TicketLine;

import java.text.DecimalFormat;

/**
 * Created by svirch_n on 28/04/16
 * Last edited at 15:25.
 */
public class PdfTicketRow extends PdfRow {

    public PdfTicketRow(Context context, TableLayout root) {
        super(context, root);
    }


    public void setTicket(TicketLine ticketLine) {
        String[] elements = {
                ticketLine.getProduct().getLabel(),
                "x " + formatQuantity(ticketLine.getQuantity()),
                formatRound(ticketLine.getProductExcTax()),
                formatTax(ticketLine.getDiscountRate()),
                formatTax(ticketLine.getProduct().getTaxRate()),
                formatRound(ticketLine.getTotalDiscPIncTax())
        };
        addElements(elements);
    }

    private String formatRound(double value) {
        return String.valueOf(round(value));
    }

    private String formatQuantity(double quantity) {
        DecimalFormat decimalFormat = new DecimalFormat("#");
        return decimalFormat.format(quantity);
    }

    public void setHeader() {
        String[] elements = {
                "Product Name",
                "Quantity",
                "Unit HT",
                "Discount",
                "TVA",
                "Total TTC"
        };
        addElements(elements);
    }
}
