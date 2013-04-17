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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Composition with selected products for ticket */
public class CompositionInstance extends Product {

    private Composition compo;
    private Map<Composition.Group, Product> components;

    public CompositionInstance(Product product, Composition compo) {
        super(product.id, product.label, product.price, product.taxId,
                product.taxRate);
        this.compo = compo;
        this.components = new HashMap<Composition.Group, Product>();
    }

    @Override
    public String getLabel() {
        String lbl = this.label + " (";
        boolean empty = true;
        for (Composition.Group g : components.keySet()) {
            lbl += this.components.get(g).getLabel() + ", ";
            empty = false;
        }
        if (!empty) {
            lbl = lbl.substring(0, lbl.length() - 2);
        }
        lbl += ")";
        return lbl;
    }

    public boolean isFull() {
        return this.components.keySet().size() == compo.getGroups().size();
    }

    public void setProduct(Composition.Group g, Product p) {
        this.components.put(g, p);
    }

    public List<Product> getProducts() {
        List<Product> ret = new ArrayList<Product>();
        for (Composition.Group g : this.components.keySet()) {
            ret.add(this.components.get(g));
        }
        return ret;
    }
}
