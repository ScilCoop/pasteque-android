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

import android.content.Context;
import android.graphics.Bitmap;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.interfaces.Item;
import fr.pasteque.client.utils.CalculPrice;
import org.json.JSONException;
import org.json.JSONObject;

public class Product implements Serializable, Item {

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

    @Override
    public Type getType() {
        return Type.Product;
    }

    private double getPrice() {
        return CalculPrice.round(this.price);
    }

    public String getLabel() {
        return this.label;
    }

    public String getBarcode() {
        return this.barcode;
    }


    private double _getGenericPrice(double price, double discount, int binaryMask) {
        return CalculPrice.getGenericPrice(price, discount, this.taxRate, binaryMask);
    }

    double getGenericPrice(TariffArea area, int binaryMask) {
        return _getGenericPrice(getPrice(area), this.discountRate, binaryMask);
    }

    double getGenericPrice(TariffArea area, double discount, int binaryMask) {
        return _getGenericPrice(getPrice(area),
                CalculPrice.mergeDiscount(this.discountRate, discount), binaryMask);
    }

    public double getPrice(TariffArea area) {
        if (area != null && area.hasPrice(this.id)) {
            return area.getPrice(this.id).doubleValue();
        }
        return this.price;
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

    @Override
    public Bitmap getImage(Context ctx) {
        return ImagesData.getProductImage(this.getId());
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
        if (!(o instanceof Product)) {
            return false;
        }
        if (((Product) o).id != null) {
            return ((Product) o).id.equals(this.id);
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
