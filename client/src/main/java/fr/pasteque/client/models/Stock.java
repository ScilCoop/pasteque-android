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

public class Stock implements Serializable {

    private String productId;
    private Double quantity;
    private Double security;
    private Double max;

    public Stock(String productId, Double quantity, Double security,
            Double max) {
        this.productId = productId;
        this.quantity = quantity;
        this.security = security;
        this.max = max;
    }

    public String getProductId() {
        return this.productId;
    }
    public boolean isManaged() {
        return this.quantity != null;
    }
    public Double getQuantity() {
        return this.quantity;
    }
    public Double getSecurity() {
        return this.security;
    }
    public boolean hasSecurityLevel() {
        return this.security != null;
    }
    public Double getMaxLevel() {
        return this.max;
    }
    public boolean hasMaxLevel() {
        return this.max != null;
    }

    public static Stock fromJSON(JSONObject o) throws JSONException {
        String productId = o.getString("productId");
        Double quantity = null;
        if (!o.isNull("qty")) {
            quantity = o.getDouble("qty");
        }
        Double security = null;
        if (!o.isNull("security")) {
            security = o.getDouble("security");
        }
        Double max = null;
        if (!o.isNull("max")) {
            max = o.getDouble("max");
        }
        return new Stock(productId, quantity, security, max);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Stock
                && ((Stock)o).productId.equals(this.productId);
    }

}
