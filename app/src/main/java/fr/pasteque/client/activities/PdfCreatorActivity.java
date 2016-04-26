package fr.pasteque.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.pasteque.client.Password;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.document.Invoice;
import fr.pasteque.client.widgets.CompanyView;
import fr.pasteque.client.widgets.Pdf;

import java.util.List;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:50.
 */
public class PdfCreatorActivity extends TrackedActivity {

    public static final String TICKET_TAG = "TICKET_TAG";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Ticket ticket = (Ticket) bundle.getSerializable(PdfCreatorActivity.TICKET_TAG);
            if (ticket != null) {
                fillInvoice(ticket);

            }
        }
    }

    private void fillInvoice(Ticket ticket) {
        setContentView(R.layout.pdf_invoice_viewer);
        ((CompanyView) findViewById(R.id.inner_company)).setCompany(Pasteque.getCompany());
        if (ticket.getCustomer() != null) {
            ((CompanyView) findViewById(R.id.outer_company)).setCompany(ticket.getCustomer().getCompany());
        } else {
            ((CompanyView) findViewById(R.id.outer_company)).noCompany();
        }
        fillLines(ticket.getLines());
    }

    private void fillLines(List<TicketLine> lines) {
        LinearLayout content = (LinearLayout) findViewById(R.id.lines);
        for (TicketLine each : lines) {
            RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.pdf_item_line, null);
            setText(layout, R.id.name, each.getProduct().getLabel());
            setText(layout, R.id.qtt, "x " + each.getQuantity());
            setText(layout, R.id.taxe, String.valueOf(each.getTotalTaxCost(each.getDiscountRate())));
            setText(layout, R.id.price, String.valueOf(each.getTotalIncTax()));
            content.addView(layout);
        }
    }

    private void setText(RelativeLayout layout, int id, String text) {
        ((TextView)layout.findViewById(id)).setText(text);
    }

    public Pdf getPdf() {
        return (Pdf) findViewById(R.id.pdf);
    }
}
