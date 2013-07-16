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

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Product implements Serializable {

    protected String id;
    protected String label;
    protected double price;
    protected String taxId;
    protected double taxRate;
    protected boolean scaled;

    public Product(String id, String label, double price,
                   String taxId,double taxRate, boolean scaled) {
        this.id = id;
        this.label = label;
        this.price = price;
        this.taxId = taxId;
        this.taxRate = taxRate;
        this.scaled = scaled;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public double getPrice() {
        return this.price;
    }

    public double getPrice(TariffArea area) {
        if (area != null && area.hasPrice(this.id)) {
            return area.getPrice(this.id);
        } else {
            return this.price;
        }
    }

    public double getTaxedPrice() {
        return this.price + this.price * this.taxRate;
    }

    public double getTaxedPrice(TariffArea area) {
        if (area != null && area.hasPrice(this.id)) {
            return area.getPrice(this.id) * (1 + this.taxRate);
        } else {
            return this.getTaxedPrice();
        }
    }

    public double getTaxPrice() {
        return this.price * this.taxRate;
    }

    public double getTaxPrice(TariffArea area) {
        if (area != null && area.hasPrice(this.id)) {
            return area.getPrice(this.id) * this.taxRate;
        } else {
            return this.getTaxPrice();
        }
    }

    public String getTaxId() {
        return this.taxId;
    }

    public double getTaxRate() {
        return this.taxRate;
    }

    public boolean isScaled() {
        return this.scaled;
    }

    public static Product fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        String label = o.getString("label");
        double price = o.getDouble("price_sell");
        JSONObject jtax = o.getJSONObject("tax_cat").getJSONArray("taxes").getJSONObject(0);
        double tax = jtax.getDouble("rate");
        String taxId = jtax.getString("id");
        boolean scaled = o.getBoolean("scaled");
        return new Product(id, label, price, taxId, tax, scaled);
    }
    
    public JSONObject toJSON(TariffArea area) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("label", this.label);
        if (area != null && area.hasPrice(this.id)) {
            o.put("price", area.getPrice(this.id));
        } else {
            o.put("price", this.price);
        }
        o.put("taxId", this.taxId);
        o.put("taxRate", this.taxRate);
        o.put("scaled", this.scaled);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Product && ((Product)o).id.equals(this.id);
    }

    @Override
    public String toString() {
        return this.label + " (" + this.id + ")";
    }
}
