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
    private double quantity;

    public TicketLine(Product p, double quantity) {
        this.product = p;
        this.quantity = quantity;
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

    /** Add or remove quantity.
     * @return true if possible, false if quantity reaches 0 or below.
     */
    public boolean adjustQuantity(double qty) {
        if (this.quantity + qty > 0) {
            this.quantity += qty;
            return true;
        } else {
            return false;
        }
    }

    public double getTotalPrice() {
        return this.product.getTaxedPrice() * this.quantity;
    }
    public double getTotalPrice(TariffArea area) {
        if (area != null && area.hasPrice(this.product.getId())) {
            return area.getPrice(this.product.getId())
                    * (1 + this.product.getTaxRate()) * this.quantity;
        } else {
            return this.getTotalPrice();
        }
    }

    public double getSubtotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    public double getTaxPrice() {
        return this.product.getTaxPrice() * this.quantity;
    }

    public JSONObject toJSON(TariffArea area) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("product", this.product.toJSON(area));
        o.put("quantity", this.quantity);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TicketLine
            && ((TicketLine)o).getProduct().equals(this.product)
            && ((TicketLine)o).getQuantity() == this.quantity;
    }
}
