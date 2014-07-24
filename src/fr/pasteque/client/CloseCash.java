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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.pasteque.client.data.CashArchive;
import fr.pasteque.client.data.CashData;
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
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.StocksAdapter;

public class CloseCash extends TrackedActivity {

    private static final String LOG_TAG = "Pasteque/Cash";

    private ListView stockList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.close_cash);
        if (Configure.getStockLocation(this) != "") {
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
        } else {
            // Hide stock
            this.findViewById(R.id.stock_container).setVisibility(View.GONE);
        }
        // Set z ticket info
        ZTicket z = new ZTicket(this);
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
    }

    /** Check running tickets to show an alert if there are some.
     * @return True if cash can be closed safely. False otherwise.
     */
    private static boolean preCloseCheck(Context ctx) {
        return !SessionData.currentSession(ctx).hasRunningTickets();
    }

    /** Show confirm dialog before closing. */
    private static void closeConfirm(final TrackedActivity ctx) {
        // Show confirmation alert
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        b.setTitle(R.string.close_running_ticket_title);
        b.setMessage(R.string.close_running_ticket_message);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setNegativeButton(android.R.string.cancel, null);
        b.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeCash(ctx);
                    }
                });
        b.show();
    }

    public void closeAction(View w) {
        if (CloseCash.preCloseCheck(this)) {
            CloseCash.closeCash(this);
        } else {
            CloseCash.closeConfirm(this);
        }
    }

    /** Effectively close the cash */
    private static void closeCash(TrackedActivity ctx) {
        CashData.currentCash(ctx).closeNow();
        CashData.dirty = true;
        // Archive and create a new cash
        try {
            CashArchive.archiveCurrent(ctx);
            CashData.clear(ctx);
            CashData.setCash(new Cash(Configure.getMachineName(ctx)));
            ReceiptData.clear(ctx);
            try {
                CashData.save(ctx);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save cash", e);
                Error.showError(R.string.err_save_cash, ctx);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to archive cash", e);
        }
        SessionData.clear(ctx);
        Start.backToStart(ctx);
    }

    public static void close(TrackedActivity caller) {
        Intent i = new Intent(caller, CloseCash.class);
        caller.startActivity(i);
    }
}
