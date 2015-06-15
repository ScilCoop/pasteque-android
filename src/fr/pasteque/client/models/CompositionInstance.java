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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Composition with selected products for ticket */
public class CompositionInstance extends Product {

    private Composition compo;
    private Map<Composition.Group, Product> components;

    public CompositionInstance(Product product, Composition compo) {
        super(product.id, product.label, product.barcode, product.price,
                product.taxId, product.taxRate, product.scaled,
                product.hasImage, product.discountRate, product.discountRateEnabled);
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompositionInstance)) {
        System.out.println("instance");
            return false;
        }
        CompositionInstance ci = (CompositionInstance) o;
        boolean equals = super.equals(o);
        System.out.println(equals + " super");
        if (this.components.keySet().size() != ci.components.keySet().size()) {
        System.out.println("keyset");
            return false;
        }
        for (Composition.Group g : this.components.keySet()) {
            if (!ci.components.containsKey(g)) {
            System.out.println("contains");
                return false;
            }
            Product p1 = this.components.get(g);
            Product p2 = ci.components.get(g);
            equals &= p1.equals(p2);
            System.out.println(equals + " " + p1 + "/" + p2);
        }
        return equals;
    }
}
