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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOError;
import java.io.IOException;

import fr.pasteque.client.data.Data;
import fr.pasteque.client.data.DataSavable.ReceiptData;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.widgets.ReceiptsAdapter;
import fr.pasteque.client.printing.PrinterConnection;

public class ReceiptSelect extends TrackedActivity
implements AdapterView.OnItemClickListener, Handler.Callback {

    private static final String LOG_TAG = "Pasteque/ReceiptSelect";

    private ListView list;
    private ProgressDialog printing;
    private PrinterConnection printer;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        setContentView(R.layout.receipt_select);
        this.list = (ListView) this.findViewById(R.id.receipts_list);
        this.list.setAdapter(new ReceiptsAdapter(Data.Receipt.getReceipts(this)));
        this.list.setOnItemClickListener(this);
        // Init printer connection
        this.printer = new PrinterConnection(new Handler(this));
        try {
            if (!this.printer.connect(this)) {
                this.printer = null;
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Unable to connect to printer", e);
            Error.showError(R.string.print_no_connexion, this);
            // Set null to cancel printing
            this.printer = null;
        }
    }

    @Override
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

    @Override
	public void onItemClick(AdapterView parent, View v,
            int position, long id) {
        final Receipt r = Data.Receipt.getReceipts(this).get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String label = this.getString(R.string.ticket_label,
                r.getTicket().getTicketId());
        builder.setTitle(label);
        String[] items = new String[] { this.getString(R.string.print),
                this.getString(R.string.delete) };
        builder.setItems(items, new DialogInterface.OnClickListener() {
           @Override
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
        if (Data.Receipt.hasReceipts()) {
            ReceiptSelect.this.list.setAdapter(new ReceiptsAdapter(Data.Receipt.getReceipts(this)));
        } else {
            ReceiptSelect.this.finish();
        }
    }

    private void delete(Receipt r) {
        Data.Receipt.getReceipts(this).remove(r);
        try {
            Data.Receipt.save(ReceiptSelect.this);
        } catch(IOError e) {
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

    @Override
	public boolean handleMessage(Message m) {
        switch (m.what) {
        case PrinterConnection.PRINT_DONE:
            if (this.printing != null) {
                this.printing.dismiss();
                this.printing = null;
            }
            break;
        case PrinterConnection.PRINT_CTX_ERROR:
            Log.w(LOG_TAG, "Unable to connect to printer");
            if (this.printing != null) {
                this.printing.dismiss();
                this.printing = null;
            }
            Error.showError(R.string.print_no_connexion, this);
            break;
        }
        return true;
    }
}
