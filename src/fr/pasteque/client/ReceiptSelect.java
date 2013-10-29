/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.pasteque.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import java.io.IOException;

import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.widgets.ReceiptsAdapter;
import fr.pasteque.client.printing.LKPXXPrinter;
import fr.pasteque.client.printing.Printer;
import fr.pasteque.client.utils.TrackedActivity;

public class ReceiptSelect extends TrackedActivity
implements AdapterView.OnItemClickListener, Handler.Callback {

    private static final String LOG_TAG = "Pasteque/ReceiptSelect";

    private ListView list;
    private ProgressDialog printing;
    private Printer printer;
    private int printConnectTries;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        setContentView(R.layout.receipt_select);
        this.list = (ListView) this.findViewById(R.id.receipts_list);
        this.list.setAdapter(new ReceiptsAdapter(ReceiptData.getReceipts(this)));
        this.list.setOnItemClickListener(this);
        // Set printer
        this.printConnectTries = 0;
        String prDriver = Configure.getPrinterDriver(this);
        if (!prDriver.equals("None")) {
            if (prDriver.equals("LK-PXX")) {
                this.printer = new LKPXXPrinter(this,
                                Configure.getPrinterAddress(this),
                                new Handler(this));
                try {
                    this.printer.connect();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Unable to connect to printer", e);
                    Error.showError(R.string.print_no_connexion, this);
                    // Set null to disable printing
                    this.printer = null;
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.printer != null) {
            try {
                this.printer.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onItemClick(AdapterView parent, View v,
            int position, long id) {
        final Receipt r = ReceiptData.getReceipts(this).get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String label = this.getString(R.string.ticket_label,
                r.getTicket().getLabel());
        builder.setTitle(label);
        String[] items = new String[] { this.getString(R.string.print),
                this.getString(R.string.delete) };
        builder.setItems(items, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               switch (which) {
               case 0:
                   if (ReceiptSelect.this.printer != null) {
                       print(r);
                   } else {
                       AlertDialog.Builder builder = new AlertDialog.Builder(ReceiptSelect.this);
                       builder.setMessage(R.string.no_printer);
                       builder.setNeutralButton(android.R.string.ok, null);
                       builder.show();
                   }
                   break;
               case 1:
                   delete(r);
                   break;
               }
           }
        });
        builder.show();
    }

    private void refreshList() {
        if (ReceiptData.hasReceipts()) {
            ReceiptSelect.this.list.setAdapter(new ReceiptsAdapter(ReceiptData.getReceipts(this)));
        } else {
            ReceiptSelect.this.finish();
        }
    }

    private void delete(Receipt r) {
        // Check if the receipt changed customer prepaid or debt
        boolean custDirty = false;
        Ticket t = r.getTicket();
        for (Payment p : r.getPayments()) {
            PaymentMode mode = p.getMode();
            if (mode.isDebt()) {
                // Remove customer debt
                t.getCustomer().addDebt(-1 * p.getAmount());
                custDirty = true;
            }
            if (mode.isPrepaid()) {
                // Refill prepaid account
                t.getCustomer().addPrepaid(p.getAmount());
                custDirty = true;
            }
        }
        Catalog cat = CatalogData.catalog(this);
        for (TicketLine l : t.getLines()) {
            Product p = l.getProduct();
            if (cat.getProducts(cat.getPrepaidCategory()).contains(p)) {
                // Unfill prepaid account
                double prepaid = p.getTaxedPrice() * l.getQuantity();
                t.getCustomer().addPrepaid(-1 * prepaid);
                custDirty = true;
            }
        }
        if (custDirty) {
            int index = CustomerData.customers.indexOf(t.getCustomer());
            CustomerData.customers.remove(index);
            CustomerData.customers.add(index, t.getCustomer());
            try {
                CustomerData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save customers", e);
                Error.showError(R.string.err_save_customers, this);
            }
        }
        // Remove the receipt
        ReceiptData.getReceipts(this).remove(r);
        try {
            ReceiptData.save(ReceiptSelect.this);
        } catch(IOException e) {
            Log.e(LOG_TAG, "Unable to save receipts", e);
            Error.showError(R.string.err_save_receipts, ReceiptSelect.this);
        }
        this.refreshList();
    }

    private void print(Receipt r) {
        this.printer.printReceipt(r);
        this.printing = new ProgressDialog(this);
        this.printing.setIndeterminate(true);
        this.printing.setMessage(this.getString(R.string.print_printing));
        this.printing.show();
    }

    public boolean handleMessage(Message m) {
        switch (m.what) {
        case LKPXXPrinter.PRINT_DONE:
            if (this.printing != null) {
                this.printing.dismiss();
                this.printing = null;
            }
            break;
        case LKPXXPrinter.PRINT_CTX_ERROR:
            this.printConnectTries++;
            Log.w(LOG_TAG, "Unable to connect to printer");
            if (this.printConnectTries < Configure.getPrinterConnectTry(this)) {
                // Retry silently
                try {
                    this.printer.connect();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Unable to connect to printer", e);
                    if (this.printing != null) {
                        this.printing.dismiss();
                        this.printing = null;
                    }
                    Error.showError(R.string.print_no_connexion, this);
                    // Set null to cancel printing
                    this.printer = null;
                }
            } else {
                if (this.printing != null) {
                    this.printing.dismiss();
                    this.printing = null;
                }
                Log.w(LOG_TAG, "Unable to connect to printer");
                Error.showError(R.string.print_no_connexion, this);
                // set null to disable printing
                this.printer = null;
            }
            break;
        }
        return true;
    }
}
