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
import org.json.JSONException;
import org.json.JSONObject;

public class TicketLine implements Serializable {

    private Product product;
    private int quantity;

    public TicketLine(Product p, int quantity) {
        this.product = p;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return this.product;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int qty) {
        this.quantity = qty;
    }

    public void addOne() {
        this.quantity += 1;
    }

    public boolean removeOne() {
        this.quantity--;
        return this.quantity > 0;
    }

    public double getTotalPrice() {
        return this.product.getTaxedPrice() * this.quantity;
    }

    public double getSubtotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    public double getTaxPrice() {
        return this.product.getTaxPrice() * this.quantity;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("product", this.product.toJSON());
        o.put("quantity", this.quantity);
        return o;
    }
}
