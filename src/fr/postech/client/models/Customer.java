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

public class Customer implements Serializable {

    private String id;
    private String name;
    private String card;
    private double maxDebt;
    private double currDebt;

    public Customer(String id, String name, String card, double maxDebt,
            double currDebt) {
        this.id = id;
        this.name = name;
        this.card = card;
        this.maxDebt = maxDebt;
        this.currDebt = currDebt;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static Customer fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        String name = o.getString("disp_name");
        String card = o.getString("card");
        double maxDebt = o.getDouble("max_debt");
        double currDebt = o.getDouble("curr_debt");
        return new Customer(id, name, card, maxDebt, currDebt);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
