package fr.pasteque.client.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.fragments.CustomerInfoDialog;
import fr.pasteque.client.fragments.CustomerSelectDialog;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.widgets.CompanyView;
import fr.pasteque.client.widgets.pdf.Pdf;
import fr.pasteque.client.widgets.pdf.PdfTaxeRow;
import fr.pasteque.client.widgets.pdf.PdfTicketRow;
import fr.pasteque.client.widgets.pdf.PdfTotalRow;

import java.util.List;
import java.util.Map;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:50.
 */
public class PdfCreatorActivity extends TrackedActivity implements CustomerSelectDialog.Listener, CustomerInfoDialog.CustomerListener {

    public static final String TICKET_TAG = "TICKET_TAG";
    private static final int HEADER_PADDING = 10;
    private static final int EDIT = 1;
    private View.OnClickListener addCustomerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addCustomer();
        }
    };
    private Customer customer;

    private void addCustomer() {
        CustomerSelectDialog dialog = CustomerSelectDialog.newInstance(true);
        dialog.setDialogListener(PdfCreatorActivity.this);
        dialog.show(getFragmentManager(), CustomerSelectDialog.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_creator_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_menu_customer_add:
                this.createCustomer();
                break;
            case R.id.ab_menu_customer_list:
                this.addCustomer();
                break;
            case R.id.ab_menu_customer_edit:
                this.editCustomer();
                break;
        }
        return true;
    }

    private void createCustomer() {
        CustomerInfoDialog customerInfoDialog = CustomerInfoDialog.newInstance(true, null);
        customerInfoDialog.setDialogCustomerListener(this);
        customerInfoDialog.show(getFragmentManager());
    }

    private void editCustomer() {
        CustomerInfoDialog customerInfoDialog = CustomerInfoDialog.newInstance(true, this.customer);
        customerInfoDialog.setDialogCustomerListener(this);
        customerInfoDialog.show(getFragmentManager());
    }

    private CompanyView outerCompany;

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
        outerCompany = (CompanyView) findViewById(R.id.outer_company);
        if (ticket.getCustomer() != null) {
            outerCompany.setCompany(ticket.getCustomer().getCompany());
        } else {
            outerCompany.setListener(this.addCustomerListener);
            outerCompany.noCompany();
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
        for (Double taxe : taxeValue.keySet()) {
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
        for (TicketLine each : lines) {
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

    @Override
    public void onCustomerPicked(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            outerCompany.setCompany(customer.getCompany());
        } else {
            outerCompany.setCompany(null);
        }
    }

    @Override
    public void onCustomerCreated(Customer customer) {
        this.customer = customer;
        outerCompany.setCompany(customer.getCompany());
    }
}
