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

import java.io.Serializable;
import java.util.Currency;

import org.json.JSONException;
import org.json.JSONObject;

public class Payment implements Serializable, JSONable {

    private PaymentMode mode;
    private double amount;
    private double given;
    /** Used for equality */
    private int innerId;
    transient private Payment backPayment;

    public Payment(PaymentMode mode, double amount, double given) {
        this.mode = mode;
        this.amount = amount;
        this.given = given;
        this.innerId = (int) (Math.random() * Integer.MAX_VALUE);
    }

    /**
     * Get negative payment for overflow. May be null. It always has
     * amount = given and are negative.
     */
    public Payment getBackPayment() {
        if (this.backPayment != null) {
            // Already computed (though not serialized)
            return this.backPayment;
        }
        // Try to generate it
        double overflow = this.given - this.amount;
        if (overflow < 0.005) {
            return null; // float arithmetic imprecision
        }
        PaymentMode backMode = this.mode.getReturnMode(overflow);
        if (backMode == null) {
            return null;
        }
        this.backPayment = new Payment(backMode, -overflow, -overflow);
        return this.backPayment;
    }

    public PaymentMode getMode() {
        return this.mode;
    }

    public double getAmount() {
        return this.amount;
    }
    public double getGiven() {
        return this.given;
    }
    
    public Currency getCurrency() {
    	return Currency.getInstance("EUR");
    }
    
    public int getInnerId() {
    	return innerId;
    }
    
    /** Get payment exceedent */
    public double getGiveBack() {
        // TODO: rename function
        return this.given - this.amount;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("amount", this.given);
        o.put("type", this.mode.getCode());
        o.put("currencyAmount", JSONObject.NULL);
        o.put("currencyId", JSONObject.NULL);
        Payment backPmt = this.getBackPayment();
        if (backPmt != null) {
            JSONObject back = new JSONObject();
            back.put("type", backPmt.mode.getCode());
            back.put("amount", backPmt.getAmount());
            o.put("back", back);
        } else {
            o.put("back", JSONObject.NULL);
        }
        return o;
    }

    @Override
	public boolean equals(Object o) {
        return o instanceof Payment && ((Payment)o).innerId == this.innerId;
    }
}
