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
import org.json.JSONException;
import org.json.JSONObject;

public class Payment implements Serializable {

    private PaymentMode mode;
    private double amount;
    private double given;
    /** Used for equality */
    private int innerId;

    public Payment(PaymentMode mode, double amount, double given) {
        this.mode = mode;
        this.amount = amount;
        this.given = given;
        this.innerId = (int) (Math.random() * Integer.MAX_VALUE);
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
    public double getGiveBack() {
        return this.given - this.amount;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("amount", this.amount);
        o.put("mode", this.mode.toJSON());
        return o;
    }

    public boolean equals(Object o) {
        return o instanceof Payment && ((Payment)o).innerId == this.innerId;
    }
}
