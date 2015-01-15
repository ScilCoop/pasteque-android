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
package fr.pasteque.client.models;

import android.content.Context;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A validated ticket */
public class Receipt implements Serializable {

    private Ticket ticket;
    private List<Payment> payments;
    private long paymentTime;
    private User cashier;

    public Receipt(Ticket t, List<Payment> p, User u) {
        this.ticket = t;
        this.payments = p;
        this.cashier = u;
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        this.paymentTime = now.getTime() / 1000;
    }

    public long getPaymentTime() {
        return this.paymentTime;
    }

    public Ticket getTicket() {
        return this.ticket;
    }

    public List<Payment> getPayments() {
        return this.payments;
    }

    public JSONObject toJSON(Context ctx) throws JSONException {
        JSONObject o = this.ticket.toJSON(false);
        JSONArray pays = new JSONArray();
        for (Payment p : this.payments) {
            pays.put(p.toJSON(ctx));
        }
        o.put("payments", pays);
        o.put("userId", this.cashier.getId());
        o.put("date", this.paymentTime);
        return o;
    }

    @Override
    public String toString() {
        return this.ticket.toString() + " by " + this.cashier.toString()
            + " at " + paymentTime;
    }
}
