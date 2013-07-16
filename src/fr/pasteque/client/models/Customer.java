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
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Customer implements Serializable {

    private String id;
    private String name;
    private String card;
    private double prepaid;
    private double maxDebt;
    private double currDebt;

    public Customer(String id, String name, String card, double prepaid,
            double maxDebt, double currDebt) {
        this.id = id;
        this.name = name;
        this.card = card;
        this.prepaid = prepaid;
        this.maxDebt = maxDebt;
        this.currDebt = currDebt;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCard() {
        return this.card;
    }

    public double getCurrDebt() {
        return this.currDebt;
    }

    public double getMaxDebt() {
        return this.maxDebt;
    }

    public void addDebt(double amount) {
        this.currDebt += amount;
    }

    public double getPrepaid() {
        return this.prepaid;
    }
    public void setPrepaid(double value) {
        this.prepaid = value;
    }
    public void addPrepaid(double amount) {
        this.prepaid += amount;
    }

    public static Customer fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        String name = o.getString("disp_name");
        String card = o.getString("card");
        double maxDebt = 0.0;
        if (!o.isNull("max_debt")) {
            maxDebt = o.getDouble("max_debt");
        }
        double currDebt = 0.0;
        if (!o.isNull("curr_debt")) {
            currDebt = o.getDouble("curr_debt");
        }
        double prepaid = o.getDouble("prepaid");
        return new Customer(id, name, card, prepaid, maxDebt, currDebt);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Customer) && this.id.equals(((Customer)o).id);
    }
}
