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
import android.widget.EditText;
import android.widget.Gallery;

import fr.postech.client.models.Ticket;
import fr.postech.client.models.PaymentMode;
import fr.postech.client.widgets.NumKeyboard;
import fr.postech.client.widgets.PaymentModesAdapter;

public class Payment extends Activity implements Handler.Callback{


    private static Ticket ticketInit;
    public static void setup(Ticket ticket) {
        ticketInit = ticket;
    }

    private Ticket ticket;

    private NumKeyboard keyboard;
    private EditText input;
    private Gallery paymentModes;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        if (state != null) {
            this.ticket = (Ticket) state.getSerializable("ticket");
        } else {
            this.ticket = ticketInit;
            ticketInit = null;
        }
        setContentView(R.layout.payments);
        this.keyboard = (NumKeyboard) this.findViewById(R.id.numkeyboard);
        keyboard.setKeyHandler(new Handler(this));
        this.input = (EditText) this.findViewById(R.id.input);
        this.paymentModes = (Gallery) this.findViewById(R.id.payment_modes);
        PaymentModesAdapter adapt = new PaymentModesAdapter(PaymentMode.MODES);
        this.paymentModes.setAdapter(adapt);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ticket", this.ticket);
    }

    public boolean handleMessage(Message m) {
        this.input.setText(String.valueOf(this.keyboard.getValue()));
        return true;
    }
}