package fr.pasteque.client.widgets.pdf;

import android.content.Context;
import android.widget.TableLayout;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.utils.CalculPrice;

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
                "x " + ticketLine.getQuantity(),
                String.valueOf(round(ticketLine.getProductIncTax())),
                String.valueOf(round(ticketLine.getTotalIncTax()))
        };
        addElements(elements);
    }

    public void setHeader() {
        String[] elements = {
                "Product Name",
                "Quantity",
                "Unit TTC",
                "Total TTC"
        };
        addElements(elements);
    }
}
