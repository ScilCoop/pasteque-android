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
    private static final int CUSTOM_NONE = 0;
    private static final int CUSTOM_PRICE = 1;
    private static final int CUSTOM_DISCOUNT = 2;

    private int id;
    private Product product;
    private String productId;
    private double quantity;
    private double lineCustomDiscount;
    private double lineCustomPrice;
    private int customFlags;

    public TicketLine(Product p, double quantity) {
        this.product = p;
        this.quantity = quantity;
        this.customFlags = CUSTOM_NONE;
    }

    public TicketLine(Product p, double quantity, int customFlags,
                      double customPrice, double customDiscount) {
        this.product = p;
        this.quantity = quantity;
        this.customFlags = customFlags;
        if ((customFlags & CUSTOM_PRICE) == CUSTOM_PRICE) this.lineCustomPrice = customPrice;
        if ((customFlags & CUSTOM_DISCOUNT) == CUSTOM_DISCOUNT)
            this.lineCustomDiscount = customDiscount;
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

    public void setCustomDiscount(double discountRate) {
        this.customFlags |= CUSTOM_DISCOUNT;
        this.lineCustomDiscount = discountRate;
    }

    public void setCustomPrice(double customPrice) {
        this.customFlags |= CUSTOM_PRICE;
        this.lineCustomPrice = customPrice;
    }

    public void removeCustomPrice() {
        this.customFlags &= ~CUSTOM_PRICE;
    }

    public boolean hasCustomPrice() {
        return (this.customFlags & CUSTOM_PRICE) == CUSTOM_PRICE;
    }

    public boolean hasCustomDiscount() {
        return (this.customFlags & CUSTOM_DISCOUNT) == CUSTOM_DISCOUNT;
    }

    public boolean isCustom() {
        return !(this.customFlags == CUSTOM_NONE);
    }

    public double getUndiscountedPrice() {
        return getUndiscountedPrice(null);
    }

    // With Vat
    public double getUndiscountedPrice(TariffArea area) {
        if ((this.customFlags & CUSTOM_PRICE) == CUSTOM_PRICE) {
            return this.lineCustomPrice;
        }
        return this.product.getPriceIncTax(area) * this.quantity;
    }

    public double getTotalPrice() {
        return this.getTotalPrice(null);
    }

    // With Vat
    public double getTotalPrice(TariffArea area) {
        return getUndiscountedPrice(area) * (1 - getDiscountRate());
    }

    // Without Vat
    public double getTotalPriceExcTax(TariffArea area) {
        return getTotalPrice(area) / (1 + this.product.getTaxRate());
    }

    // Just vat
    public double getTaxCost(TariffArea area) {
        return getTotalPriceExcTax(area) * this.product.getTaxRate();
    }

    public double getDiscountRate() {
        double discount = 0;
        if ((this.customFlags & CUSTOM_DISCOUNT) == CUSTOM_DISCOUNT) {
            discount = this.lineCustomDiscount;
        } else if (this.product.isDiscountRateEnabled()) {
            discount = this.product.getDiscountRate();
        }
        return discount;
    }

    public static TicketLine fromJSON(Context context, JSONObject o)
            throws JSONException {
        Catalog catalog = CatalogData.catalog(context);
        String productId = o.getString("productId");
        double quantity = o.getDouble("quantity");
        int customFlags = o.getInt("customFlags");
        double customPrice = o.getDouble("price");
        double discountRate = o.getDouble("discountRate");

        return new TicketLine(catalog.getProduct(productId), quantity,
                customFlags, customPrice, discountRate);
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
        o.put("customFlags", this.customFlags);
        if ((this.customFlags & CUSTOM_PRICE) == CUSTOM_PRICE) {
            double price = (this.quantity == 0) ? (0) : (this.lineCustomPrice / this.quantity);
            price = price / (1 + this.product.getTaxRate());
            o.put("price", price);
        } else {
            o.put("price", this.product.getPriceExcTax(area));
        }
        o.put("taxId", this.product.getTaxId());
        o.put("discountRate", this.lineCustomDiscount);
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
        if (res && (res = (this.customFlags == l.customFlags)) && this.customFlags != CUSTOM_NONE) {
            res = this.lineCustomPrice == l.lineCustomPrice
                    && this.lineCustomDiscount == l.lineCustomDiscount;
        }
        return res;
    }
}
