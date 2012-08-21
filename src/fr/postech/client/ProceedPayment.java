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
import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.TicketInput;
import fr.postech.client.data.CashData;
import fr.postech.client.data.ReceiptData;
import fr.postech.client.data.SessionData;
import fr.postech.client.models.Ticket;
import fr.postech.client.models.Payment;
import fr.postech.client.models.PaymentMode;
import fr.postech.client.models.Receipt;
import fr.postech.client.models.Session;
import fr.postech.client.models.User;
import fr.postech.client.widgets.NumKeyboard;
import fr.postech.client.widgets.PaymentModesAdapter;

public class ProceedPayment extends Activity
    implements Handler.Callback, AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = "POS-Tech/ProceedPayment";

    private static Ticket ticketInit;
    public static void setup(Ticket ticket) {
        ticketInit = ticket;
    }

    private Ticket ticket;
    private List<Payment> payments;
    private PaymentMode currentMode;

    private NumKeyboard keyboard;
    private EditText input;
    private Gallery paymentModes;
    private TextView ticketTotal;
    private TextView ticketRemaining;
    private TextView giveBack;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        if (state != null) {
            this.ticket = (Ticket) state.getSerializable("ticket");
            this.payments = new ArrayList<Payment>();
            int count = state.getInt("payCount");
            for (int i = 0; i < count; i++) {
                Payment p = (Payment) state.getSerializable("payment" + i);
                this.payments.add(p);
            }
        } else {
            this.ticket = ticketInit;
            ticketInit = null;
            this.payments = new ArrayList<Payment>();
        }
        setContentView(R.layout.payments);
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
        String total = this.getString(R.string.ticket_total,
                                      this.ticket.getTotalPrice());
        this.ticketTotal.setText(total);
        this.updateDisplayToMode();
        this.refreshRemaining();
        this.refreshGiveBack();
        this.refreshInput();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ticket", this.ticket);
        outState.putInt("payCount", this.payments.size());
        for (int i = 0; i < this.payments.size(); i++) {
            outState.putSerializable("payment" + i, this.payments.get(i));
        }
    }

    /** Update display to current payment mode */
    private void updateDisplayToMode() {
        if (this.currentMode.isGiveBack()) {
            this.giveBack.setVisibility(View.VISIBLE);
        } else {
            this.giveBack.setVisibility(View.GONE);
        }
    }

    private double getRemaining() {
        double paid = 0.0;
        for (Payment p : this.payments) {
            paid += p.getAmount();
        }
        return this.ticket.getTotalPrice() - paid;
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

    public void onItemSelected(AdapterView<?> parent, View v,
                               int position, long id) {
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
        if (m.what == NumKeyboard.KEY_ENTER) {
            this.validatePayment();
        } else {
            this.refreshInput();
            this.input.setSelection(this.input.getText().toString().length());
            this.refreshGiveBack();
        }
        return true;
    }

    /** Pre-payment actions */
    public void validatePayment() {
        if (this.currentMode != null) {
            double remaining = this.getRemaining();
            double amount = Double.parseDouble(this.input.getText().toString());
            // Use remaining when money is given back
            if (this.currentMode.isGiveBack() && amount > remaining) {
                amount = remaining;
            }
            boolean proceed = true;
            if (amount >= remaining) {
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
        double amount = Double.parseDouble(this.input.getText().toString());
        Payment p = new Payment(this.currentMode, amount);
        this.payments.add(p);
        this.refreshRemaining();
        this.resetInput();
        Toast t = Toast.makeText(this, R.string.payment_done,
                                 Toast.LENGTH_SHORT);
        t.show();
    }
    
    /** Save ticket and return to a new one */
    private void closePayment() {
        // Create and save the receipt
        User u = Session.currentSession.getUser();
        Receipt r = new Receipt(this.ticket, this.payments, u);
        ReceiptData.addReceipt(r);
        try {
            ReceiptData.save(this);
        } catch(IOException e) {
            Log.e(LOG_TAG, "Unable to save receipts", e);
            Error.showError(R.string.err_save_receipts, this);
        }
        // Return to a new ticket edit
        TicketInput.requestTicketSwitch(Session.currentSession.newTicket());
        this.finish();
        new Thread() {
            public void run() {
                try {
                    SessionData.saveSession(Session.currentSession, ProceedPayment.this);
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Unable to save session", ioe);
                    Error.showError(R.string.err_save_session,
                                    ProceedPayment.this);
                }
            }
        }.start();
    }
}