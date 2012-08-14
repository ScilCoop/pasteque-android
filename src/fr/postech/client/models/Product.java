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

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class Product implements Serializable {

    private String label;
    private double price;
    private double taxRate;

    public Product(String label, double price, double taxRate) {
        this.label = label;
        this.price = price;
        this.taxRate = taxRate;
    }

    public String getLabel() {
        return this.label;
    }

    public double getPrice() {
        return this.price;
    }

    public double getTaxedPrice() {
        return this.price + this.price * this.taxRate;
    }

    public double getTaxPrice() {
        return this.price * this.taxRate;
    }

    public Drawable getIcon() {
        return null;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("label", this.label);
        o.put("price", this.price);
        o.put("tax", this.taxRate);
        return o;
    }
}
