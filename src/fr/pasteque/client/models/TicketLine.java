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

import android.content.Context;

import fr.pasteque.client.data.CatalogData;

public class TicketLine implements Serializable {

    private int id;
    private Product product;
    private String productId;
    private double quantity;
    private double discountRate;
    private double lineCustomPrice;
    private boolean bHasCustomPrice;

    public TicketLine(Product p, double quantity) {
        this.product = p;
        this.quantity = quantity;
        this.bHasCustomPrice = false;
    }

    public TicketLine(Product p, double quantity, boolean bHasCustomPrice,
                      double customPrice, double discountRate) {
        this.product = p;
        this.quantity = quantity;
        this.bHasCustomPrice = bHasCustomPrice;
        if (bHasCustomPrice) this.lineCustomPrice = customPrice;
        if (discountRate != 0) this.discountRate = discountRate;
    }

    public Product getProduct() {
        return this.product;
    }

    public double getQuantity() {
        return this.quantity;
    }

    public void setQuantity(double qty) {
        this.quantity = qty;
    }

    public void addOne() {
        this.quantity += 1;
    }

    public boolean removeOne() {
        this.quantity--;
        return this.quantity > 0;
    }

    /**
     * Add or remove quantity.
     */
    public void adjustQuantity(double qty) {
        this.quantity += qty;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public void setCustomPrice(double customPrice) {
        this.lineCustomPrice = customPrice;
        this.bHasCustomPrice = true;
    }

    public void removeCustomPrice() {
        this.bHasCustomPrice = false;
    }

    public boolean hasCustomPrice() {
        return this.bHasCustomPrice;
    }

    public boolean isCustom() {
        return this.bHasCustomPrice || this.discountRate != 0;
    }

    public double getTotalPrice() {
        return this.getTotalPrice(null);
    }

    public double getTotalPrice(TariffArea area) {
        if (this.bHasCustomPrice) {
            return this.lineCustomPrice;
        }
        return this.product.getTaxedPrice(area) * this.quantity;
    }

    public double getSubtotalPrice(TariffArea area) {
        if (this.bHasCustomPrice) {
            return this.lineCustomPrice;
        }
        return this.product.getPrice(area) * this.quantity;
    }

    public double getTaxPrice(TariffArea area) {
        if (this.bHasCustomPrice) {
            return this.lineCustomPrice;
        }
        return this.product.getTaxPrice(area) * this.quantity;
    }

    public double getDiscountRate() {
        return this.discountRate;
    }

    public static TicketLine fromJSON(Context context, JSONObject o)
            throws JSONException {
        Catalog catalog = CatalogData.catalog(context);
        String productId = o.getString("productId");
        double quantity = o.getDouble("quantity");
        boolean bHasCustomPrice = o.getBoolean("customPrice");
        double customPrice = o.getDouble("price");
        double discountRate = o.getDouble("discountRate");

        return new TicketLine(catalog.getProduct(productId), quantity,
                bHasCustomPrice, customPrice, discountRate);
    }

    public JSONObject toJSON(String sharedTicketId, TariffArea area)
            throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        if (sharedTicketId != null) {
            o.put("sharedTicketId", sharedTicketId);
        }
        o.put("productId", this.product.getId());
        o.put("taxId", this.product.getTaxId());
        o.put("attributes", JSONObject.NULL);
        o.put("quantity", this.quantity);
        o.put("customPrice", this.bHasCustomPrice);
        if (this.bHasCustomPrice) {
            double price = (this.quantity == 0) ? (0) : (this.lineCustomPrice / this.quantity);
            price = price / (1 + this.product.getTaxRate());
            o.put("price", price);
        } else {
            o.put("price", this.product.getPrice(area));
        }
        o.put("taxId", this.product.getTaxId());
        o.put("discountRate", this.discountRate);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        boolean res = o instanceof TicketLine;
        TicketLine l = null;
        if (res) {
            l = ((TicketLine) o);
            res = l.getProduct().equals(this.product)
                    && l.getQuantity() == this.quantity;
        }
        if (res && (res = (this.bHasCustomPrice == l.bHasCustomPrice)) && this.bHasCustomPrice) {
            res = this.lineCustomPrice == l.lineCustomPrice
                    && this.discountRate == l.discountRate;
        }
        return res;
    }
}
