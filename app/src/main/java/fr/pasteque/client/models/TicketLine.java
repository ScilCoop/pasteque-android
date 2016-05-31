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
import fr.pasteque.client.utils.Tuple;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


/**
 * TicketLine hold all prices.
 * The method name is logical for a better readability.
 * <p>
 * get|1|2|3|tax
 * <p>
 * 1: target
 * - 'Product': for the product
 * - 'Total': for the product and his quantity
 * <p>
 * 2: Discounts
 * - '': no discount
 * - 'DiscP': productDiscount
 * - 'Disc': productDiscount + ticketDiscount
 * <p>
 * 3: Taxes
 * - 'Inc': Include Tax
 * - 'Exc': Exclude Tax
 * <p>
 * Ex: getTotalDiscPIncTax() = total price with the productDiscount including taxes
 */
public class TicketLine implements Serializable {
    private static final int CUSTOM_NONE = 0;
    private static final int CUSTOM_PRICE = 1;
    private static final int CUSTOM_DISCOUNT = 2;

    // Used in clone and canMerge method
    private Product product;
    private double quantity;
    private TariffArea tariffArea;
    private double lineCustomDiscount;
    private double lineCustomPrice;
    private int customFlags;
    // End used in clone and canMerge method

    private TicketLine() {
    }

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

    /**
     * @return {code}1{/code} if quantity is > 0, -1 if not
     */
    private double getAnArticle() {
        if (this.quantity > 0) {
            return 1;
        } else {
            return -1;
        }
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

    private double applyQuantity(double price) {
        return CalculPrice.round(price * this.quantity);
    }

    public double getTotalIncTax() {
        return applyQuantity(this.getProductIncTax());
    }

    public double getTotalExcTax() {
        return applyQuantity(this.getProductExcTax());
    }

    public double getTotalDiscPIncTax() {
        return CalculPrice.applyDiscount(getTotalIncTax(), getDiscountRate());
    }

    public double getTotalDiscIncTax(double discountRate) {
        double discount = CalculPrice.mergeDiscount(getDiscountRate(), discountRate);
        return applyQuantity(CalculPrice.applyDiscount(getProductIncTax(), discount));
    }

    public double getTotalDiscPExcTax() {
        return applyQuantity(CalculPrice.applyDiscount(getProductExcTax(), getDiscountRate()));
    }

    public double getTotalDiscExcTax(double ticketDiscount) {
        return applyQuantity(CalculPrice.applyDiscount(getProductDiscPExcTax(), ticketDiscount));
    }

    public double getProductTaxCost(double ticketDiscount) {
        return CalculPrice.getTaxCost(getProductDiscExcTax(ticketDiscount), this.product.getTaxRate());
    }

    public double getTotalTaxCost(double ticketDiscount) {
        return applyQuantity(getProductTaxCost(ticketDiscount));
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
        double customPrice = o.getDouble("price");
        double discountRate = o.getDouble("discountRate");
        int customFlags = 0;
        Product product = catalog.getProduct(productId);
        if (product == null) {
            throw new JSONException("Product is null");
        }
        customFlags = CUSTOM_NONE;
        if (product.getPrice(area) != customPrice) {
            customFlags |= CUSTOM_PRICE;
        }
        if (discountRate != 0.0) {
            customFlags |= CUSTOM_DISCOUNT;
        }
        return new TicketLine(catalog.getProduct(productId), quantity, area,
                customFlags, customPrice, discountRate);
    }

    public JSONObject toJSON()
            throws JSONException {
        JSONObject o = new JSONObject();
        o.put("productId", this.product.getId());
        o.put("productLabel", this.product.getLabel());
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

    public TicketLine getRefundLine() {
        return new TicketLine(getProduct(), -getQuantity(), tariffArea, customFlags, lineCustomPrice, lineCustomDiscount);
    }

    /**
     * Pure method, creates 2 Ticketlines.
     *
     * @return a first TicketLine with the desired article number, a second ticketLine with the remaining article number or null of none left
     * @throws CannotSplitScaledProductException if product is a scaled product
     */
    public Tuple<TicketLine, TicketLine> splitTicketLineArticle() throws CannotSplitScaledProductException {
        if (this.getProduct().isScaled()) {
            throw new CannotSplitScaledProductException();
        }
        TicketLine first = this.clone();
        double splitQuantity = getAnArticle();
        first.quantity = splitQuantity;
        if (this.getQuantity() - splitQuantity == 0) {
            return new Tuple<>(first, null);
        }
        TicketLine second = this.clone();
        second.quantity -= first.quantity;
        return new Tuple<>(first, second);
    }


    protected TicketLine clone() {
        TicketLine result = new TicketLine();
        result.product = product;
        result.quantity = quantity;
        result.tariffArea = tariffArea;
        result.lineCustomDiscount = lineCustomDiscount;
        result.lineCustomPrice = lineCustomPrice;
        result.customFlags = customFlags;
        return result;
    }

    /**
     * merge the given ticketline with the current instance
     *
     * @param ticketLine the ticketline to merge
     */
    public void merge(TicketLine ticketLine) {
        if (canMerge(ticketLine)) {
            this.adjustQuantity(ticketLine.getQuantity());
        }
    }

    /**
     * A ticketline is mergeable if all is field are equels except the quantity
     *
     * @param ticketLine
     * @return
     */
    public boolean canMerge(TicketLine ticketLine) {
        return ticketLine.product.equals(product)
                && (tariffArea == null || ticketLine.tariffArea.equals(tariffArea))
                && ticketLine.lineCustomDiscount == lineCustomDiscount
                && ticketLine.lineCustomPrice == lineCustomPrice
                && ticketLine.customFlags == customFlags;
    }

    public class CannotSplitScaledProductException extends Throwable {
    }
}
