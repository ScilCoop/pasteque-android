/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client;

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

import fr.postech.client.data.ReceiptData;
import fr.postech.client.models.Receipt;
import fr.postech.client.widgets.ReceiptsAdapter;
import fr.postech.client.printing.LKPXXPrinter;
import fr.postech.client.printing.Printer;
import fr.postech.client.utils.TrackedActivity;

public class ReceiptSelect extends TrackedActivity
implements AdapterView.OnItemClickListener, Handler.Callback {

    private static final String LOG_TAG = "POS-TECH/ReceiptSelect";

    private ListView list;
    private ProgressDialog printing;
    private Printer printer;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        setContentView(R.layout.receipt_select);
        this.list = (ListView) this.findViewById(R.id.receipts_list);
        this.list.setAdapter(new ReceiptsAdapter(ReceiptData.getReceipts(this)));
        this.list.setOnItemClickListener(this);
        // Set printer
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
            if (this.printing != null) {
                this.printing.dismiss();
                this.printing = null;
            }
            Log.w(LOG_TAG, "Unable to connect to printer");
            Error.showError(R.string.print_no_connexion, this);
            // set null to disable printing
            this.printer = null;
            break;
        }
        return true;
    }
}
