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
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;
import com.payleven.payment.api.OpenTransactionDetailsCompletedStatus;
import com.payleven.payment.api.PaylevenApi;
import com.payleven.payment.api.PaylevenResponseListener;
import com.payleven.payment.api.PaymentCompletedStatus;
import com.payleven.payment.api.TransactionRequest;
import com.payleven.payment.api.TransactionRequestBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import fr.pasteque.client.TicketInput;
import fr.pasteque.client.data.CashData;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.printing.PrinterConnection;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.User;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.NumKeyboard;
import fr.pasteque.client.widgets.PaymentsAdapter;
import fr.pasteque.client.widgets.PaymentModesAdapter;

public class ProceedPayment extends TrackedActivity
    implements Handler.Callback, AdapterView.OnItemSelectedListener,
               PaymentEditListener {
    
    private static final String LOG_TAG = "Pasteque/ProceedPayment";
    private static final String PAYLEVEN_API_KEY = "edaffb929bd34aa78122b2d15a36a5c7";
    private static final int SCROLL_WHAT = 90; // Be sure not to conflict with keyboard whats
    
    private static Ticket ticketInit;
    public static void setup(Ticket ticket) {
        ticketInit = ticket;
    }

    private Ticket ticket;
    private List<Payment> payments;
    private PaymentMode currentMode;
    private boolean paymentClosed;
    private PrinterConnection printer;

    private NumKeyboard keyboard;
    private EditText input;
    private Gallery paymentModes;
    private TextView ticketTotal;
    private TextView ticketRemaining;
    private TextView giveBack;
    private ListView paymentsList;
    private SlidingDrawer slidingDrawer;
    private ImageView slidingHandle;
    private ScrollView scroll;
    private Handler scrollHandler;
    private boolean printEnabled;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        boolean open = false;
        if (state != null) {
            this.ticket = (Ticket) state.getSerializable("ticket");
            this.payments = new ArrayList<Payment>();
            int count = state.getInt("payCount");
            for (int i = 0; i < count; i++) {
                Payment p = (Payment) state.getSerializable("payment" + i);
                this.payments.add(p);
            }
            open = state.getBoolean("drawerOpen");
            this.printEnabled = state.getBoolean("printEnabled");
        } else {
            this.ticket = ticketInit;
            ticketInit = null;
            this.payments = new ArrayList<Payment>();
            this.printEnabled = true;
        }
        setContentView(R.layout.payments);
        this.scroll = (ScrollView) this.findViewById(R.id.scroll);
        this.scrollHandler = new Handler(this);
        this.keyboard = (NumKeyboard) this.findViewById(R.id.numkeyboard);
        keyboard.setKeyHandler(new Handler(this));
        this.input = (EditText) this.findViewById(R.id.input);
        this.giveBack = (TextView) this.findViewById(R.id.give_back);
        this.ticketTotal = (TextView) this.findViewById(R.id.ticket_total);
        this.ticketRemaining = (TextView) this.findViewById(R.id.ticket_remaining);
        this.paymentModes = (Gallery) this.findViewById(R.id.payment_modes);
        PaymentModesAdapter adapt = new PaymentModesAdapter(PaymentMode.defaultModes(this));
        this.paymentModes.setAdapter(adapt);
        this.paymentModes.setOnItemSelectedListener(this);
        this.paymentModes.setSelection(0, false);
        this.currentMode = PaymentMode.defaultModes(this).get(0);
        String total = this.getString(R.string.ticket_total,
                                      this.ticket.getTotalPrice());

        this.slidingHandle = (ImageView) this.findViewById(R.id.handle);
        this.slidingDrawer = (SlidingDrawer) this.findViewById(R.id.drawer);
        this.slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
                @Override
                    public void onDrawerOpened() {
                    slidingHandle.setImageResource(R.drawable.slider_close);
                }
            });
        slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
                @Override
                    public void onDrawerClosed() {
                    slidingHandle.setImageResource(R.drawable.slider_open);
                }
            });
        if (open) {
            this.slidingDrawer.open();
        }

        this.paymentsList = (ListView) this.findViewById(R.id.payments_list);
        PaymentsAdapter padapt = new PaymentsAdapter(this.payments, this);
        this.paymentsList.setAdapter(padapt);

        this.ticketTotal.setText(total);
        this.updateDisplayToMode();
        this.refreshRemaining();
        this.refreshGiveBack();
        this.refreshInput();
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
        // Init Payleven API
        PaylevenApi.configure("edaffb929bd34aa78122b2d15a36a5c7");
        // Update UI based upon settings
        View paylevenBtn = this.findViewById(R.id.btnPayleven);
        if (Configure.getPayleven(this)) {
            paylevenBtn.setVisibility(View.VISIBLE);
        } else {
            paylevenBtn.setVisibility(View.INVISIBLE);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ticket", this.ticket);
        outState.putInt("payCount", this.payments.size());
        for (int i = 0; i < this.payments.size(); i++) {
            outState.putSerializable("payment" + i, this.payments.get(i));
        }
        outState.putBoolean("drawerOpen", this.slidingDrawer.isOpened());
        outState.putBoolean("printEnabled", this.printEnabled);
    }

    /** Update display to current payment mode */
    private void updateDisplayToMode() {
        if (this.currentMode.isGiveBack() || this.currentMode.isDebt()
                || this.currentMode.isPrepaid()) {
            this.giveBack.setVisibility(View.VISIBLE);
        } else {
            this.giveBack.setVisibility(View.INVISIBLE);
        }
    }

    private double getRemainingPrepaid() {
        if (this.ticket.getCustomer() != null) {
            double prepaid = this.ticket.getCustomer().getPrepaid();
            // Substract prepaid payments
            for (Payment p : this.payments) {
                if (p.getMode().isPrepaid()) {
                    prepaid -= p.getAmount();
                }
            }
            // Add ordered refills
            for (TicketLine l : this.ticket.getLines()) {
                Product p = l.getProduct();
                Catalog cat = CatalogData.catalog(this);
                if (cat.getProducts(cat.getPrepaidCategory()).contains(p)) {
                    prepaid += p.getTaxedPrice() * l.getQuantity();
                }
            }
            return prepaid;
        } else {
            return 0.0;
        }
    }

    private double getRemaining() {
        double paid = 0.0;
        for (Payment p : this.payments) {
            paid += p.getAmount();
        }
        return this.ticket.getTotalPrice() - paid;
    }

    /** Get entered amount. If money is given back, amount is the final sum
     * (not the given one).
     */
    private double getAmount() {
        double remaining = this.getRemaining();
        double amount = remaining;
        if (!this.input.getText().toString().equals("")) {
            amount = Double.parseDouble(this.input.getText().toString());
        }
        // Use remaining when money is given back
        if (this.currentMode.isGiveBack() && amount > remaining) {
            amount = remaining;
        }
        return amount;
    }
    private double getGiven() {
        double remaining = this.getRemaining();
        double given = remaining;
        if (!this.input.getText().toString().equals("")) {
            given = Double.parseDouble(this.input.getText().toString());
        }
        return given;
    }

    private void refreshRemaining() {
        double remaining = this.getRemaining();
        String strRemaining = this.getString(R.string.ticket_remaining,
                                             remaining);
        this.ticketRemaining.setText(strRemaining);
    }

    private void refreshGiveBack() {
        if (this.currentMode.isGiveBack()) {
            double overflow = this.keyboard.getValue() - this.getRemaining();
            if (overflow > 0.0) {
                String back = this.getString(R.string.payment_give_back,
                                             overflow);
                this.giveBack.setText(back);
            } else {
                String back = this.getString(R.string.payment_give_back,
                                             0.0);
                this.giveBack.setText(back);
            }
        }
        if (this.currentMode.isCustAssigned()
                && this.ticket.getCustomer() == null) {
            this.giveBack.setText(R.string.payment_no_customer);
        } else {
            if (this.currentMode.isDebt()
                    && this.ticket.getCustomer() != null) {
                double debt = this.ticket.getCustomer().getCurrDebt();
                for (Payment p : this.payments) {
                    if (p.getMode().isDebt()) {
                        debt += p.getAmount();
                    }
                }
                double maxDebt = this.ticket.getCustomer().getMaxDebt();
                String debtStr = this.getString(R.string.payment_debt,
                        debt, maxDebt);
                this.giveBack.setText(debtStr);
            } else if (this.currentMode.isPrepaid()
                    && this.ticket.getCustomer() != null) {
                double prepaid = this.getRemainingPrepaid();
                String strPrepaid = this.getString(R.string.payment_prepaid,
                        prepaid);
                this.giveBack.setText(strPrepaid);
            }
        }
    }

    private void refreshInput() {
        this.input.setHint(String.format("%.2f", this.getRemaining()));
        this.input.setText(this.keyboard.getRawValue());
    }

    public void resetInput() {
        this.keyboard.clear();
        this.refreshInput();
        this.refreshGiveBack();
    }

    public void correct(View v) {
        this.keyboard.correct();
        this.refreshInput();
        this.refreshGiveBack();
        this.input.setSelection(this.input.getText().toString().length());
    }

    public void clear(View v) {
        this.resetInput();
    }

    public void sendToPayleven(View v) {
        int amount = (int) (Math.round(this.getAmount() * 100)); // in cents
        TransactionRequestBuilder builder = new TransactionRequestBuilder(amount, Currency.getInstance("EUR"));
        TransactionRequest request = builder.createTransactionRequest();
        String orderId = "42";
        PaylevenApi.initiatePayment(this, orderId, request);
    }

    private void scrollToKeyboard() {
        if (this.scroll != null) {
            this.scroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View v,
                               int position, long id) {
        if (this.currentMode != null) {
            // Not first auto-selection, trigger scroll
            // Cancel previous scroll call and send a new delayed one
            this.scrollHandler.removeMessages(SCROLL_WHAT);
            this.scrollHandler.sendEmptyMessageDelayed(SCROLL_WHAT, 800);
        }
        PaymentModesAdapter adapt = (PaymentModesAdapter)
            this.paymentModes.getAdapter();
        PaymentMode mode = (PaymentMode) adapt.getItem(position);
        this.currentMode = mode;
        this.resetInput();
        this.updateDisplayToMode();
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public boolean handleMessage(Message m) {
        switch (m.what) {
        case NumKeyboard.KEY_ENTER:
            this.validatePayment();
            break;
        case SCROLL_WHAT:
            this.scrollToKeyboard();
            break;
        case PrinterConnection.PRINT_DONE:
            this.end();
            break;
        case PrinterConnection.PRINT_CTX_ERROR:
            Exception e = (Exception) m.obj;
            Log.w(LOG_TAG, "Unable to connect to printer", e);
            if (this.paymentClosed) {
                Toast t = Toast.makeText(this,
                        R.string.print_no_connexion, Toast.LENGTH_LONG);
                t.show();
                this.end();
            } else {
                Error.showError(R.string.print_no_connexion, this);
                // Set null to cancel printing
                this.printer = null;
            }
            break;
        case PrinterConnection.PRINT_CTX_FAILED:
            // Give up
            if (this.paymentClosed) {
                Toast t = Toast.makeText(this, R.string.print_no_connexion,
                        Toast.LENGTH_LONG);
                t.show();
                this.end();
            } else {
                Error.showError(R.string.print_no_connexion, this);
                // Set null to disable printing
                this.printer = null;
            }
            break;
        default:
            this.refreshInput();
            this.input.setSelection(this.input.getText().toString().length());
            this.refreshGiveBack();
            break;
        }
        return true;
    }

    public void deletePayment(Payment p) {
        this.payments.remove(p);
        ((PaymentsAdapter)this.paymentsList.getAdapter()).notifyDataSetChanged();
        this.refreshRemaining();
    }


    /** Pre-payment actions */
    public void validatePayment() {
        if (this.currentMode != null) {
            double remaining = this.getRemaining();
            // Get amount from entered value (default is remaining)
            double amount = this.getAmount();
            // Check for debt and cust assignment
            if (this.currentMode.isCustAssigned()
                    && this.ticket.getCustomer() == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.payment_no_customer);
                builder.setNeutralButton(android.R.string.ok, null);
                builder.show();
                return;
            }
            if (this.currentMode.isDebt()) {
                double debt = this.ticket.getCustomer().getCurrDebt();
                for (Payment p : this.payments) {
                    if (p.getMode().isDebt()) {
                        debt += p.getAmount();
                    }
                }
                if (debt + amount > this.ticket.getCustomer().getMaxDebt()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.payment_debt_exceeded);
                    builder.setNeutralButton(android.R.string.ok, null);
                    builder.show();
                    return;
                }
            }
            if (this.currentMode.isPrepaid()) {
                double prepaid = this.getRemainingPrepaid();
                if (prepaid < amount) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.payment_no_enough_prepaid);
                    builder.setNeutralButton(android.R.string.ok, null);
                    builder.show();
                    return;
                }
            }
            boolean proceed = true;
            if (remaining - amount < 0.005) {
                // Confirm payment end
                proceed = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.confirm_payment_end)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes,                                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                proceedPayment();
                                closePayment();
                            }
                        })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)  {
                                dialog.cancel();
                            }
                        })
                    .show();
            }
            if (proceed) {
                this.proceedPayment();
            }
        }
    }

    /** Register the payment */
    private void proceedPayment() {
        double amount = this.getAmount();
        Payment p = new Payment(this.currentMode, amount, this.getGiven());
        this.payments.add(p);
        ((PaymentsAdapter)this.paymentsList.getAdapter()).notifyDataSetChanged();
        this.refreshRemaining();
        this.resetInput();
        Toast t = Toast.makeText(this, R.string.payment_done,
                                 Toast.LENGTH_SHORT);
        t.show();
    }
    
    /** Save ticket and return to a new one */
    private void closePayment() {
        // Create and save the receipt and remove from session
        Session currSession = SessionData.currentSession(this);
        User u = currSession.getUser();
        final Receipt r = new Receipt(this.ticket, this.payments, u);
        ReceiptData.addReceipt(r);
        try {
            ReceiptData.save(this);
        } catch(IOException e) {
            Log.e(LOG_TAG, "Unable to save receipts", e);
            Error.showError(R.string.err_save_receipts, this);
        }
        currSession.closeTicket(this.ticket);
        try {
            SessionData.saveSession(ProceedPayment.this);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Unable to save session", ioe);
            Error.showError(R.string.err_save_session,
                           ProceedPayment.this);
        }
        // Update customer debt
        boolean custDirty = false;
        for (Payment p : this.payments) {
            if (p.getMode().isDebt()) {
                this.ticket.getCustomer().addDebt(p.getAmount());
                custDirty = true;
            }
        }
        if (this.ticket.getCustomer() != null) {
            Customer cust = this.ticket.getCustomer();
            if (this.getRemainingPrepaid() != cust.getPrepaid()) {
                cust.setPrepaid(this.getRemainingPrepaid());
                custDirty = true;
            }
        }
        if (custDirty) {
            int index = CustomerData.customers.indexOf(this.ticket.getCustomer());
            CustomerData.customers.remove(index);
            CustomerData.customers.add(index, this.ticket.getCustomer());
            try {
                CustomerData.save(this);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to save customers", e);
                Error.showError(R.string.err_save_customers, this);
            }
        }
        this.paymentClosed = true;
        // Check printer
        if (this.printer != null && this.printEnabled == true) {
            printer.printReceipt(r);
            ProgressDialog progress = new ProgressDialog(this);
            progress.setIndeterminate(true);
            progress.setMessage(this.getString(R.string.print_printing));
            progress.show();
        } else {
            this.end();
        }
    }

    private void end() {
        Session currSession = SessionData.currentSession(this);
        // Return to a new ticket edit
        switch (Configure.getTicketsMode(this)) {
        case Configure.SIMPLE_MODE:
            TicketInput.requestTicketSwitch(currSession.newTicket());
            this.finish();
            break;
        case Configure.STANDARD_MODE:
            if (!currSession.hasTicket()) {
                TicketInput.requestTicketSwitch(currSession.newTicket());
                this.finish();
                break;
            } // else open ticket input like in restaurant mode
        case Configure.RESTAURANT_MODE:
            Intent i = new Intent(this, TicketSelect.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivityForResult(i, TicketSelect.CODE_TICKET);
            break;
        }
    }

    protected void onActivityResult (int requestCode, int resultCode,
                                     Intent data) {
        PaylevenApi.handleIntent(requestCode, data,
                new PaylevenResultHandler());
        switch (requestCode) {
        case TicketSelect.CODE_TICKET:
            switch (resultCode) {
            case Activity.RESULT_CANCELED:
                // Back to start
                Intent i = new Intent(this, Start.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(i);
                break;
            case Activity.RESULT_OK:
                TicketInput.requestTicketSwitch(SessionData.currentSession(this).getCurrentTicket());
                this.finish();
            break;
            }
        }
    }

    private class PaylevenResultHandler implements PaylevenResponseListener {
        public void onPaymentFinished(String orderId,
                TransactionRequest originalRequest, Map<String, String> result,
                PaymentCompletedStatus status) {
            switch (status) {
            case AMOUNT_TOO_LOW:
                Error.showError(R.string.payment_card_rejected,
                        ProceedPayment.this);
                break;
            case API_KEY_DISABLED:
            case API_KEY_NOT_FOUND:
            case API_KEY_VERIFICATION_ERROR:
                Error.showError(R.string.err_payleven_key, ProceedPayment.this);
                break;
            case ANOTHER_API_CALL_IN_PROGRESS:
                Error.showError(R.string.err_payleven_concurrent_call,
                        ProceedPayment.this);
                break;
            case API_SERVICE_ERROR:
            case API_SERVICE_FAILED:
            case ERROR:
            case PAYMENT_ALREADY_EXISTS:
                Error.showError(R.string.err_payleven_general,
                        ProceedPayment.this);
                break;
            case CARD_AUTHORIZATION_ERROR:
                Error.showError(R.string.payment_card_rejected,
                        ProceedPayment.this);
                break;
            case INVALID_CURRENCY:
            case WRONG_COUNTRY_CODE:
                Error.showError(R.string.err_payleven_forbidden,
                        ProceedPayment.this);
                break;
            case SUCCESS:
                ProceedPayment.this.proceedPayment();
                break;
            }
         }

         public void onNoPaylevenResponse(Intent data) {
         }

         public void onOpenTransactionDetailsFinished(String orderId,
                 Map<String, String> transactionData,
                 OpenTransactionDetailsCompletedStatus status) {
         }

         public void onOpenSalesHistoryFinished() {
         }
    }

    private static final int MENU_PRINT = 0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem print = menu.add(Menu.NONE, MENU_PRINT, 0,
                this.getString(R.string.menu_print_enabled));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem print = menu.findItem(MENU_PRINT);
        if (this.printer != null) {
            print.setEnabled(true);
            if (this.printEnabled) {
                print.setTitle(R.string.menu_print_enabled);
                print.setIcon(R.drawable.ic_menu_print_enabled);
            } else {
                print.setTitle(R.string.menu_print_disabled);
                print.setIcon(R.drawable.ic_menu_print_disabled);
            }
        } else {
            print.setEnabled(false);
            print.setTitle(R.string.menu_print_not_available);
            print.setIcon(R.drawable.ic_menu_print_disabled);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_PRINT:
            this.printEnabled = !this.printEnabled;
            break;
        }
        return true;
    }
}
