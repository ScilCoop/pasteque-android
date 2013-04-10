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
package fr.postech.client.models;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A validated ticket */
public class Receipt implements Serializable {

    private Ticket ticket;
    private List<Payment> payments;
    private long paymentTime;
    private User cashier;
    private Customer customer;

    public Receipt(Ticket t, List<Payment> p, User u, Customer c) {
        this.ticket = t;
        this.payments = p;
        this.cashier = u;
        this.customer = c;
        this.paymentTime = System.currentTimeMillis() / 1000;
    }

    public Ticket getTicket() {
        return this.ticket;
    }

    public List<Payment> getPayments() {
        return this.payments;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("ticket", this.ticket.toJSON());
        JSONArray pays = new JSONArray();
        for (Payment p : this.payments) {
            pays.put(p.toJSON());
        }
        o.put("payments", pays);
        o.put("cashier", this.cashier.toJSON());
        o.put("customer", this.customer.getId());
        o.put("date", this.paymentTime);
        return o;
    }

    @Override
    public String toString() {
        return this.ticket.toString() + " by " + this.cashier.toString()
            + " at " + paymentTime;
    }
}
