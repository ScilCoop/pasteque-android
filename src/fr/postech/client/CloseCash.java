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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.postech.client.data.CashData;
import fr.postech.client.data.CatalogData;
import fr.postech.client.data.ReceiptData;
import fr.postech.client.data.SessionData;
import fr.postech.client.data.StockData;
import fr.postech.client.models.Cash;
import fr.postech.client.models.Product;
import fr.postech.client.models.Receipt;
import fr.postech.client.models.Session;
import fr.postech.client.models.Stock;
import fr.postech.client.models.Ticket;
import fr.postech.client.models.TicketLine;
import fr.postech.client.widgets.StocksAdapter;

public class CloseCash extends Activity {

    private static final String LOG_TAG = "POS-Tech/Cash";

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
                        Stock upd = new Stock(s.getProductId(), oldQty - qty,
                                null, null);
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
    }

    /** Check running tickets to show an alert if there are some.
     * @return True if cash can be closed safely. False otherwise.
     */
    private static boolean preCloseCheck(Context ctx) {
        return !SessionData.currentSession(ctx).hasRunningTickets();
    }

    /** Show confirm dialog before closing. */
    private static void closeConfirm(final Context ctx) {
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

    private static void closeCash(Context ctx) {
        CashData.currentCash(ctx).closeNow();
        CashData.dirty = true;
        try {
            CashData.save(ctx);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to save cash", e);
            Error.showError(R.string.err_save_cash, ctx);
        }
        SessionData.clear(ctx);
        Start.backToStart(ctx);
    }

    public static void close(Activity caller) {
        if (Configure.getStockLocation(caller) != "") {
            Intent i = new Intent(caller, CloseCash.class);
            caller.startActivity(i);
        } else {
            if (CloseCash.preCloseCheck(caller)) {
                CloseCash.closeCash(caller);
            } else {
                CloseCash.closeConfirm(caller);
            }
        }
    }
}
