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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.models.Ticket;
import fr.postech.client.models.Payment;
import fr.postech.client.models.PaymentMode;
import fr.postech.client.widgets.NumKeyboard;
import fr.postech.client.widgets.PaymentModesAdapter;

public class ProceedPayment extends Activity
    implements Handler.Callback, AdapterView.OnItemSelectedListener {


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
        this.ticketTotal = (TextView) this.findViewById(R.id.ticket_total);
        this.ticketRemaining = (TextView) this.findViewById(R.id.ticket_remaining);
        this.paymentModes = (Gallery) this.findViewById(R.id.payment_modes);
        PaymentModesAdapter adapt = new PaymentModesAdapter(PaymentMode.defaultModes(this));
        this.paymentModes.setAdapter(adapt);
        this.paymentModes.setOnItemSelectedListener(this);
        String total = this.getString(R.string.ticket_total,
                                      this.ticket.getTotalPrice());
        this.ticketTotal.setText(total);
        this.refreshRemaining();
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

    private void refreshRemaining() {
        double paid = 0.0;
        for (Payment p : this.payments) {
            paid += p.getAmount();
        }
        double remaining = this.ticket.getTotalPrice() - paid;
        String strRemaining = this.getString(R.string.ticket_remaining,
                                             remaining);
        this.ticketRemaining.setText(strRemaining);
    }

    public void resetInput() {
        this.keyboard.clear();
        this.input.setText("");
    }

    public void onItemSelected(AdapterView<?> parent, View v,
                               int position, long id) {
        PaymentModesAdapter adapt = (PaymentModesAdapter)
            this.paymentModes.getAdapter();
        PaymentMode mode = (PaymentMode) adapt.getItem(position);
        this.currentMode = mode;
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public boolean handleMessage(Message m) {
        if (m.what == NumKeyboard.KEY_ENTER) {
            this.validatePayment();
        } else {
            this.input.setText(String.valueOf(this.keyboard.getValue()));
        }
        return true;
    }

    public void validatePayment() {
        if (this.currentMode != null) {
            double amount = Double.parseDouble(this.input.getText().toString());
            Payment p = new Payment(this.currentMode, amount);
            this.payments.add(p);
            this.refreshRemaining();
            this.resetInput();
        }
    }
}