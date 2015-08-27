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


import fr.pasteque.client.utils.CalculPrice.Type;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.utils.CalculPrice;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


/**
 * TicketLine hold all prices.
 * The method name is logical for a better readability.
 *
 * get|1|2|3|tax
 *
 * 1: target
 * - 'Product': for the product
 * - 'Total': for the product and his quantity
 *
 * 2: Discounts
 * - '': no discount
 * - 'DiscP': productDiscount
 * - 'Disc': productDiscount + ticketDiscount
 *
 * 3: Taxes
 * - 'Inc': Include Tax
 * - 'Exc': Exclude Tax
 *
 * Ex: getTotalDiscPIncTax() = total price with the productDiscount including taxes
 */
public class TicketLine implements Serializable {
    private static final int CUSTOM_NONE = 0;
    private static final int CUSTOM_PRICE = 1;
    private static final int CUSTOM_DISCOUNT = 2;

    private int id;
    private Product product;
    private String productId;
    private double quantity;
    private TariffArea tariffArea;
    private double lineCustomDiscount;
    private double lineCustomPrice;
    private int customFlags;

    public TicketLine(Product p, double quantity, TariffArea tariffArea) {
        this.setTicketLine(p, quantity, tariffArea, CUSTOM_NONE);
    }

    public TicketLine(Product p, double quantity, TariffArea tariffArea, int customFlags,
                      double customPrice, double customDiscount) {
        this.setTicketLine(p, quantity, tariffArea, customFlags);
        if ((customFlags & CUSTOM_PRICE) == CUSTOM_PRICE) this.lineCustomPrice = customPrice;
        if ((customFlags & CUSTOM_DISCOUNT) == CUSTOM_DISCOUNT)
            this.lineCustomDiscount = customDiscount;
    }

    private void setTicketLine(Product p, double quantity, TariffArea tariffArea, int customFlags) {
        this.product = p;
        this.quantity = quantity;
        this.tariffArea = tariffArea;
        this.customFlags = customFlags;
    }

    public Product getProduct() {
        return this.product;
    }

    public double getQuantity() {
        return this.quantity;
    }

    public double getArticlesNumber() {
        return Math.abs(quantity);
    }

    public void setQuantity(double qty) {
        this.quantity = qty;
    }

    public void addOneProduct() {
        this.quantity += 1;
    }

    public void addOneProductReturn() {
        this.quantity -= 1;
    }

    public boolean removeOneProduct() {
        this.quantity--;
        return this.quantity > 0;
    }

    public boolean removeOneProductReturn() {
        this.quantity++;
        return this.quantity < 0;
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

    public double getTotalIncTax() {
        return this.getProductIncTax() * this.quantity;
    }

    public double getTotalExcTax() {
        return this.getProductExcTax() * this.quantity;
    }

    public double getTotalDiscPIncTax() {
        return CalculPrice.applyDiscount(getTotalIncTax(), getDiscountRate());
    }

    // With Vat
    public double getTotalDiscIncTax(double discountRate) {
        double discount = CalculPrice.mergeDiscount(getDiscountRate(), discountRate);
        return CalculPrice.applyDiscount(getTotalIncTax(), discount);
    }

    // Without Vat
    public double getTotalDiscPExcTax() {
        return CalculPrice.applyDiscount(getTotalExcTax(), getDiscountRate());
    }

    public double getTotalDiscExcTax(double ticketDiscount) {
        double price = getTotalDiscPExcTax();
        return CalculPrice.applyDiscount(price, ticketDiscount);
    }

    private double getProductTaxCost(double ticketDiscount) {
        return CalculPrice.getTaxCost(getProductDiscExcTax(ticketDiscount), this.product.getTaxRate());
    }

    // Just vat
    public double getTotalTaxCost(double ticketDiscount) {
        return getProductTaxCost(ticketDiscount) * this.quantity;
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

    public static TicketLine fromJSON(Context context, JSONObject o, TariffArea area)
            throws JSONException {
        Catalog catalog = Data.Catalog.catalog(context);
        String productId = o.getString("productId");
        double quantity = o.getDouble("quantity");
        int customFlags = CUSTOM_NONE;
        if (o.has("customFlags")) {
            customFlags = o.getInt("customFlags");
        }
        double customPrice = o.getDouble("price");
        double discountRate = o.getDouble("discountRate");

        return new TicketLine(catalog.getProduct(productId), quantity, area,
                customFlags, customPrice, discountRate);
    }

    public JSONObject toJSON(String sharedTicketId)
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
            o.put("price", getProductIncTax());
        } else {
            o.put("price", this.product.getPrice(this.tariffArea));
        }
        o.put("taxId", this.product.getTaxId());
        o.put("discountRate", getDiscountRate());
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

    public boolean isProductReturn() {
        return this.quantity < 0;
    }

    public double getProductIncTax() {
        if (hasCustomPrice()) {
            return this.lineCustomPrice;
        } else {
            return this.product.getGenericPrice(this.tariffArea, Type.TAXE);
        }
    }

    public double getProductExcTax() {
        if (hasCustomPrice()) {
            return CalculPrice.removeTaxe(this.lineCustomPrice, this.product.getTaxRate());
        } else {
            return this.product.getGenericPrice(this.tariffArea, Type.NONE);
        }
    }

    public double getProductDiscExcTax(double ticketDiscount) {
        double discount = CalculPrice.mergeDiscount(getDiscountRate(), ticketDiscount);
        return CalculPrice.applyDiscount(getProductExcTax(), discount);
    }

    public double getProductDiscPExcTax() {
        return CalculPrice.applyDiscount(getProductExcTax(), getDiscountRate());
    }
}
