package fr.pasteque.client.activities;

import android.os.Bundle;
import android.widget.TableLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.widgets.CompanyView;
import fr.pasteque.client.widgets.pdf.Pdf;
import fr.pasteque.client.widgets.pdf.PdfTaxeRow;
import fr.pasteque.client.widgets.pdf.PdfTicketRow;
import fr.pasteque.client.widgets.pdf.PdfTotalRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:50.
 */
public class PdfCreatorActivity extends TrackedActivity {

    public static final String TICKET_TAG = "TICKET_TAG";
    private static final int HEADER_PADDING = 10;

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
        fillTaxes(ticket);
        fillTotal(ticket);
    }

    private void fillTotal(Ticket ticket) {
        TableLayout content = (TableLayout) findViewById(R.id.ticket);
        addTotalHeader(content);
        addDivider(content);
        PdfTotalRow row = new PdfTotalRow(getApplicationContext(), content);
        row.setTotal(ticket);
    }

    private void addTotalHeader(TableLayout content) {
        PdfTotalRow row = new PdfTotalRow(getApplicationContext(), content);
        TableLayout.LayoutParams params = (TableLayout.LayoutParams) row.getLayoutParams();
        params.setMargins(0, HEADER_PADDING, 0, HEADER_PADDING);
        row.setHeader();
    }

    private void fillTaxes(Ticket ticket) {
        TableLayout content = (TableLayout) findViewById(R.id.ticket);
        addTaxesHeader(content);
        addDivider(content);
        Map<Double, Double> taxeValue = ticket.getTaxes();
        Map<Double, Double> excByTaxes = ticket.getExcByTaxes();
        for (Double taxe: taxeValue.keySet()) {
            PdfTaxeRow row = new PdfTaxeRow(getApplicationContext(), content);
            row.setTaxe(taxe, taxeValue.get(taxe), excByTaxes.get(taxe));
        }
    }

    private void addDivider(TableLayout content) {
        getLayoutInflater().inflate(R.layout.pdf_divider, content, true);
    }

    private void addTaxesHeader(TableLayout content) {
        PdfTaxeRow row = new PdfTaxeRow(getApplicationContext(), content);
        TableLayout.LayoutParams params = (TableLayout.LayoutParams) row.getLayoutParams();
        params.setMargins(0, HEADER_PADDING, 0, HEADER_PADDING);
        row.setHeader();
    }

    private void fillLines(List<TicketLine> lines) {
        TableLayout content = (TableLayout) findViewById(R.id.ticket);
        addLinesHeader(content);
        addDivider(content);
        for (TicketLine each: lines) {
            PdfTicketRow row = new PdfTicketRow(getApplicationContext(), content);
            row.setTicket(each);
        }
    }

    private void addLinesHeader(TableLayout content) {
        PdfTicketRow row = new PdfTicketRow(getApplicationContext(), content);
        row.setHeader();
    }

    public Pdf getPdf() {
        return (Pdf) findViewById(R.id.pdf);
    }
}
