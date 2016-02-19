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
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.data.*;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.models.*;
import fr.pasteque.client.activities.TrackedActivity;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.widgets.StocksAdapter;

public class CloseCash extends POSConnectedTrackedActivity {

    private static final String LOG_TAG = "Pasteque/Cash";

    private ZTicket zTicket;
    private ListView stockList;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.close_cash);
        // Compute stocks with receipts
        Map<String, Stock> stocks = Data.Stock.stocks;
        Map<String, Stock> updStocks = new HashMap<String, Stock>();
        for (Receipt r : Data.Receipt.getReceipts(this)) {
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
                Data.Catalog.catalog(this)));
        // Set z ticket info
        this.zTicket = new ZTicket(this);
        String labelPayment, valuePayment, labelTaxes, valueTaxes;
        labelPayment = valuePayment = labelTaxes = valueTaxes = "";
        Map<PaymentMode, PaymentDetail> payments = zTicket.getPayments();
        Map<Double, Double> taxBases = zTicket.getTaxBases();
        // Show z ticket data
        DecimalFormat currFormat = new DecimalFormat("#0.00");
        for (PaymentMode m : payments.keySet()) {
            labelPayment += m.getLabel() + "\n";
            valuePayment += addValuePaymentMode(currFormat, payments.get(m));
        }
        ((TextView) this.findViewById(R.id.z_payment_total_value))
                .setText(currFormat.format(zTicket.getTotal()) + " €");
        DecimalFormat rateFormat = new DecimalFormat("##0.#");
        for (Double rate : taxBases.keySet()) {
            labelTaxes += (rateFormat.format(rate * 100)
                    + (rate < 10 ? " " : "") + "%  :  "
                    + currFormat.format(taxBases.get(rate)) + "\n");
            valueTaxes += currFormat.format(taxBases.get(rate) * rate) + " €\n";
        }

        ((TextView) this.findViewById(R.id.z_label_payment_content))
                .setText(labelPayment);
        ((TextView) this.findViewById(R.id.z_value_payment_content))
                .setText(valuePayment);

        ((TextView) this.findViewById(R.id.z_label_taxes_content))
                .setText(labelTaxes);
        ((TextView) this.findViewById(R.id.z_value_taxes_content))
                .setText(valueTaxes);

        ((TextView) this.findViewById(R.id.z_subtotal_value))
                .setText(currFormat.format(zTicket.getSubtotal()) + " €");
        ((TextView) this.findViewById(R.id.z_taxes_taxes_values))
                .setText(currFormat.format(zTicket.getTaxAmount()) + " €");
        ((TextView) this.findViewById(R.id.z_taxes_total_values))
                .setText(currFormat.format(zTicket.getTotal()) + " €");

        this.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseCash.this.closeAction(v);
            }
        });
    }

    private String addValuePaymentMode(DecimalFormat format, PaymentDetail paymentDetail) {
        String result;
        result = "Income: " + format.format(paymentDetail.getIncome()) + " €\t";
        result += "Outcome: " + format.format(paymentDetail.getOutcome()) + " €\t";
        result += "Total: " + format.format(paymentDetail.getTotal()) + " €";
        return result + "\n";
    }

    /**
     * Undo temporary close operations on current cash.
     */
    private void undoClose() {
        Data.Cash.currentCash(this).setCloseInventory(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Undo checks if closed nicely the new cash doesn't have these data
        // if closed by cancel the current cash may have these data set from
        // close activities
        this.undoClose();
    }

    /**
     * Check running tickets to show an alert if there are some.
     *
     * @return True if cash can be closed safely. False otherwise.
     */
    private static boolean preCloseCheck(Context ctx) {
        return !Data.Session.currentSession(ctx).hasRunningTickets();
    }

    private boolean shouldCountCash() {
        return false; // TODO: cash count on close not supported yet
    }

    /**
     * True when the user should check the stocks
     * (when required and not already done)
     */
    private boolean shouldCheckStocks() {
        //noinspection SimplifiableIfStatement
        if (Configure.getCheckStockOnClose(this)) {
            return Data.Cash.currentCash(this).getCloseInventory() == null;
        }
        return false;
    }

    /**
     * Start check activities for result.
     *
     * @return True if a check activity has been called, false if the
     * process has ended and the cash can be closed effectively.
     */
    public boolean runCloseChecks() {
        if (this.shouldCountCash()) {
            return true; // TODO call cash check activity
/*      //TODO recode InventoryInput
        } else if (this.shouldCheckStocks()) {
            Intent i = new Intent(this, InventoryInput.class);
            InventoryInput.setup(CatalogData.catalog(this));
            this.startActivityForResult(i, 0);
            return true;
*/
        } else {
            return false;
        }
    }


    /**
     * Show confirm dialog before closing.
     */
    private void closeConfirm() {
        // Show confirmation alert
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.close_running_ticket_title);
        b.setMessage(R.string.close_running_ticket_message);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setNegativeButton(android.R.string.cancel, null);
        b.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeCash();
                    }
                });
        b.show();
    }

    public void closeAction(View w) {
        if (CloseCash.preCloseCheck(this)) {
            this.closeCash();
        } else {
            this.closeConfirm();
        }
    }

    /**
     * Do close checks and effectively close the cash
     */
    private void closeCash() {
        if (this.runCloseChecks()) {
            // Call activities for result for checks and return on closeCash() then
            return;
        }
        try {
            this.closeCashAction();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to archive cash", e);
            Error.showError(R.string.err_save_cash_register, this);
            return;
        }
        //making final copy for thread use
        final ZTicket zticket = this.zTicket;
        final CashRegister cashRegister = Data.CashRegister.current(this);
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) {
                manager.printZTicket(zticket, cashRegister);
            }
        });
        showProgressDialog();
    }

    private void showProgressDialog() {
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMessage(this.getString(R.string.print_printing));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloseCash.this.progressDialog.show();
            }
        });
    }

    /**
     * Must be used for loosing data purpose.
     * Like a disconnect action.
     */
    public static void closeCashNoMatterWhat(Context ctx) {
        Data.Cash.clear(ctx);
        int cashRegId = Data.CashRegister.current(ctx).getId();
        Data.Cash.setCash(new Cash(cashRegId));
        Data.Receipt.clear(ctx);
        try {
            Data.Cash.save(ctx);
        } catch (IOError ignore) {
        }
        Data.Session.clear(ctx);
    }

    private void closeCashAction() throws IOException {
        Data.Cash.currentCash(this).closeNow();
        Data.Cash.dirty = true;
        // Archive and create a new cash
        CashArchive.archiveCurrent();
        Data.Cash.clear(this);
        int cashRegId = Data.CashRegister.current(this).getId();
        Data.Cash.setCash(new Cash(cashRegId));
        Data.Receipt.clear(this);
        try {
            Data.Cash.save(this);
        } catch (IOError e) {
            Log.e(LOG_TAG, "Unable to save cash", e);
            Error.showError(R.string.err_save_cash, this);
        }
        Data.Session.clear(this);
    }

    public static void close(TrackedActivity caller) {
        Intent i = new Intent(caller, CloseCash.class);
        caller.startActivity(i);
    }

    /**
     * On check result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (resultCode) {
            case Activity.RESULT_CANCELED:
                // Check canceled, undo close
                this.undoClose();
                break;
            case Activity.RESULT_OK:
                if (data.hasExtra("inventory")) {
                    Inventory inv = (Inventory) data.getSerializableExtra("inventory");
                    Data.Cash.currentCash(this).setCloseInventory(inv);
                    try {
                        Data.Cash.save(this);
                    } catch (IOError e) {
                        Log.e(LOG_TAG, "Unable to save cash", e);
                        Error.showError(R.string.err_save_cash, this);
                    }
                }
                // Continue close process
                this.closeCash();
                break;
        }
    }

    @Override
    public boolean onDeviceManagerEvent(DeviceManagerEvent event) {
        switch (event.what) {
            case DeviceManagerEvent.PrintError:
                Pasteque.Log.d("Unable to connect to printer");
                askReprintOrLeave(R.string.printer_failure, false);
                break;
            case DeviceManagerEvent.PrintQueued:
                askReprintOrLeave(R.string.print_no_connexion, true);
                break;
            case DeviceManagerEvent.PrintDone:
                dismissProgressDialog();
                dismissAlertDialog();
                Start.backToStart(this);
                break;
            default:
        }
        return true;
    }

    private void askReprintOrLeave(final int messageId, final boolean queued) {
        dismissProgressDialog();
        dismissAlertDialog();
        final ZTicket zTicket = this.zTicket;
        final CashRegister cashRegister = Data.CashRegister.current(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Pasteque.getStringResource(messageId));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CloseCash.this.showProgressDialog();
                getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
                    @Override
                    public void execute(POSDeviceManager manager) throws Exception {
                        if (queued) {
                            if (!manager.reconnect()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Recursive Loop in UIThread
                                        askReprintOrLeave(messageId, true);
                                    }
                                });
                            }
                        } else {
                            manager.printZTicket(zTicket, cashRegister);
                        }
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.close_anyway, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CloseCash.this.backToStart();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void backToStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Start.backToStart(CloseCash.this);
            }
        });
    }

    private void dismissAlertDialog() {
        if (alertDialog != null){
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void dismissProgressDialog() {
        if (CloseCash.this.progressDialog != null) {
            CloseCash.this.progressDialog.dismiss();
        }
    }
}
