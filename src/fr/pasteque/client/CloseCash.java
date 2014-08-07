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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.pasteque.client.data.CashArchive;
import fr.pasteque.client.data.CashData;
import fr.pasteque.client.data.CashRegisterData;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.data.StockData;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Stock;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.printing.PrinterConnection;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.StocksAdapter;

public class CloseCash extends TrackedActivity implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/Cash";

    private PrinterConnection printer;
    private ZTicket z;
    private ListView stockList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.close_cash);
        // Compute stocks with receipts
        Map<String, Stock> stocks = StockData.stocks;
        Map<String, Stock> updStocks = new HashMap<String, Stock>();
        for (Receipt r : ReceiptData.getReceipts(this)) {
            Ticket t = r.getTicket();
            for (TicketLine l : t.getLines()) {
                Product p = l.getProduct();
                double qty = l.getQuantity();
                if (stocks.containsKey(p.getId())) {
                    Stock s = stocks.get(p.getId());
                    if (s.isManaged()) {
                        double oldQty = s.getQuantity();
                        Stock upd = new Stock(s.getProductId(),
                                oldQty - qty, null, null);
                        updStocks.put(p.getId(), upd);
                    }
                }
            }
        }
        for (String id : stocks.keySet()) {
            if (!updStocks.containsKey(id)) {
                updStocks.put(id, stocks.get(id));
            }
        }
        this.stockList = (ListView) this.findViewById(R.id.close_stock);
        this.stockList.setAdapter(new StocksAdapter(updStocks,
                        CatalogData.catalog(this)));
        // Set z ticket info
        this.z = new ZTicket(this);
        // Show z ticket data
        DecimalFormat currFormat = new DecimalFormat("#0.00");
        String html = "<h2>" + this.getString(R.string.z_payments) + "</h2>";
        for (PaymentMode m : z.getPayments().keySet()) {
            html += "<p>" + m.getLabel(this) + " "
                    + currFormat.format(z.getPayments().get(m)) + "</p>";
        }
        html += "<p><b>" + this.getString(R.string.z_total) + " "
                + currFormat.format(z.getTotal()) + "</b></p>";
        DecimalFormat rateFormat = new DecimalFormat("#0.#");
        html += "<h2>" + this.getString(R.string.z_taxes) + "</h2>";
        for (Double rate : z.getTaxBases().keySet()) {
            html += "<p>" + rateFormat.format(rate * 100) + "% "
                    + currFormat.format(z.getTaxBases().get(rate))
                    + " / " + currFormat.format(z.getTaxBases().get(rate) * rate)
                    + "</p>";
        }
        html += "<p><b>" + this.getString(R.string.z_subtotal) + " "
                + currFormat.format(z.getSubtotal()) + "</b></p>";
        html += "<p><b>" + this.getString(R.string.z_taxes) + " "
                + currFormat.format(z.getTaxAmount()) + "</b></p>";
        html += "<p><b>" + this.getString(R.string.z_total) + " "
                + currFormat.format(z.getTotal()) + "</b></p>";
        ((TextView) this.findViewById(R.id.close_z_content)).setText(Html.fromHtml(html));
        // Init printer
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

    public void onDestroy() {
        super.onDestroy();
        if (this.printer != null) {
           try {
               this.printer.disconnect();
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        this.printer = null;
    }

    /** Check running tickets to show an alert if there are some.
     * @return True if cash can be closed safely. False otherwise.
     */
    private static boolean preCloseCheck(Context ctx) {
        return !SessionData.currentSession(ctx).hasRunningTickets();
    }

    /** Show confirm dialog before closing. */
    private void closeConfirm() {
        // Show confirmation alert
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.close_running_ticket_title);
        b.setMessage(R.string.close_running_ticket_message);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setNegativeButton(android.R.string.cancel, null);
        b.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeCash();
                    }
                });
        b.show();
    }

    public void closeAction(View w) {
        if (this.preCloseCheck(this)) {
            this.closeCash();
        } else {
            this.closeConfirm();
        }
    }

    /** Effectively close the cash */
    private void closeCash() {
        CashData.currentCash(this).closeNow();
        CashData.dirty = true;
        // Archive and create a new cash
        try {
            CashArchive.archiveCurrent(this);
            CashData.clear(this);
            CashData.setCash(new Cash(Configure.getMachineName(this)));
            ReceiptData.clear(this);
            try {
                CashData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save cash", e);
                Error.showError(R.string.err_save_cash, this);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to archive cash", e);
        }
        SessionData.clear(this);
        // Check printer
        if (this.printer != null) {
            this.printer.printZTicket(this.z);
            ProgressDialog progress = new ProgressDialog(this);
            progress.setIndeterminate(true);
            progress.setMessage(this.getString(R.string.print_printing));
            progress.show();
        } else {
            Start.backToStart(this);
        }
    }

    public static void close(TrackedActivity caller) {
        Intent i = new Intent(caller, CloseCash.class);
        caller.startActivity(i);
    }

    public boolean handleMessage(Message m) {
        switch (m.what) {
        case PrinterConnection.PRINT_DONE:
            Start.backToStart(this);
            break;
        case PrinterConnection.PRINT_CTX_ERROR:
            Exception e = (Exception) m.obj;
            Log.w(LOG_TAG, "Unable to connect to printer", e);
            Toast t = Toast.makeText(this,
                    R.string.print_no_connexion, Toast.LENGTH_LONG);
            t.show();
            Start.backToStart(this);
            break;
        case PrinterConnection.PRINT_CTX_FAILED:
            // Give up
            t = Toast.makeText(this, R.string.print_no_connexion,
                    Toast.LENGTH_LONG);
            t.show();
            Start.backToStart(this);
            break;
        }
        return true;
    }

}
