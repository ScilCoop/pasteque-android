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

public class Product implements Serializable {

    protected String id;
    protected String label;
    protected double price;
    protected String taxId;
    protected double taxRate;
    protected boolean scaled;
    protected String barcode;
    protected boolean hasImage;
    protected double discountRate;
    protected boolean discountRateEnabled;

    public Product(String id, String label, String barcode, double price,
                   String taxId, double taxRate, boolean scaled, boolean hasImage,
                   double discountRate, boolean discountRateEnabled) {
        this.id = id;
        this.label = label;
        this.barcode = barcode;
        this.price = price;
        this.taxId = taxId;
        this.taxRate = taxRate;
        this.scaled = scaled;
        this.hasImage = hasImage;
        this.discountRate = discountRate;
        this.discountRateEnabled = discountRateEnabled;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public String getBarcode() {
        return this.barcode;
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

    public double getDiscountRate() {
        return this.discountRate;
    }

    public boolean isScaled() {
        return this.scaled;
    }

    public boolean hasImage() {
        return this.hasImage;
    }

    public boolean isDiscountRateEnabled() {
        return this.discountRateEnabled;
    }

    public static Product fromJSON(JSONObject o, String taxId, double taxRate)
        throws JSONException {
        String id = o.getString("id");
        String label = o.getString("label");
        String barcode = null;
        if (!o.isNull("barcode")) {
            barcode = o.getString("barcode");
        }
        double price = o.getDouble("priceSell");
        boolean scaled = o.getBoolean("scaled");
        boolean hasImage = o.getBoolean("hasImage");
        double discountRate = o.getDouble("discountRate");
        boolean discountRateEnabled = o.getBoolean("discountEnabled");
        return new Product(id, label, barcode, price, taxId, taxRate, scaled,
                hasImage, discountRate, discountRateEnabled);
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
        o.put("barcode", this.barcode);
        o.put("discountRate", this.discountRate);
        o.put("discountEnabled", this.discountRateEnabled);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Product)) {
            return false;
        }
        if (((Product)o).id != null) {
            return ((Product)o).id.equals(this.id);
        } else {
            if (this.id != null) {
                return false;
            }
            Product p = (Product) o;
            return p.price == this.price;
        }
    }

    @Override
    public String toString() {
        return this.label + " (" + this.id + ")";
    }
}
