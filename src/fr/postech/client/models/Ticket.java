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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Ticket implements Serializable {

    private String label;
    private int articles;
    private List<TicketLine> lines;
    private Customer customer;
    private TariffArea area;

    public Ticket() {
        this.lines = new ArrayList<TicketLine>();
    }

    public Ticket(String label) {
        this.lines = new ArrayList<TicketLine>();
        this.label = label;
    }

    public TariffArea getTariffArea() {
        return this.area;
    }

    public void setTariffArea(TariffArea area) {
        this.area = area;
    }

    public List<TicketLine> getLines() {
        return this.lines;
    }

    public TicketLine getLineAt(int index) {
        return this.lines.get(index);
    }

    public void addLine(Product p, int qty) {
        this.lines.add(new TicketLine(p, qty));
        this.articles += qty;
    }

    /** Adds a line with a scaled product
     * @param p the product to add
     * @param qty the number of articles to add
     * @param scale the product's weight
     */
    public void addLineProductScaled(Product p, int qty, double scale) {
        this.lines.add(new TicketLine(p, scale));
        this.articles += qty;
    }

    public void removeLine(TicketLine l) {
        Product p;
        this.lines.remove(l);
        p = l.getProduct();
        if (p.isScaled()) {
            //Removes only 1 article for a scaled product
            this.articles--;
        } else {
            this.articles -= l.getQuantity();
        }
    }

    public void addProduct(Product p) {
        for (TicketLine l : this.lines) {
            if (l.getProduct().equals(p)) {
                l.addOne();
                this.articles++;
                return;
            }
        }
        this.addLine(p, 1);
    }

    /** Adds scaled product to the ticket
     * @param p the product to add
     * @param quantity the products weight
     */
    public void addScaledProduct(Product p, double quantity) {
        for (TicketLine l : this.lines) {
            if (l.getProduct().equals(p)) {
                l.adjustQuantity(quantity);
                return;
            }
        }
        this.addLineProductScaled(p, 1, quantity);
    }

    public void adjustQuantity(TicketLine l, int qty) {
        for (TicketLine li : this.lines) {
            if (li.equals(l)) {
                if (li.adjustQuantity(qty)) {
                    this.articles += qty;
                } else {
                    this.removeLine(li);
                }
                break;
            }
        }
    }

    /** Adjusts the weight of a scaled product
     * @param l the ticket's line of the product to modify
     * @param scale the modify weight
     */
    public void adjustScale(TicketLine l, double scale) {
        for (TicketLine li : this.lines) {
            if (li.equals(l)) {
                if (!li.adjustQuantity(scale)) {
                    this.removeLine(li);
                }
            break;
            }
        }
    }

    public int getArticlesCount() {
        return this.articles;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTotalPrice(this.area);
        }
        return total;
    }

    public double getSubTotalPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getSubtotalPrice();
        }
        return total;
    }

    public double getTaxPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTaxPrice();
        }
        return total;
    }

    public Map<Double, Double> getTaxes() {
        Map<Double, Double> taxes = new HashMap<Double, Double>();
        for (TicketLine l : this.lines) {
            double rate = l.getProduct().getTaxRate();
            double amount = l.getTaxPrice();
            if (taxes.containsKey(rate)) {
                amount += taxes.get(rate);
            }
            taxes.put(rate, amount);
        }
        return taxes;
    }

    public String getLabel() {
        return this.label;
    }

    public boolean isEmpty() {
        return this.lines.size() == 0;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer c) {
        this.customer = c;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("label", this.label);
        if (this.customer != null) {
            o.put("customer", this.customer.getId());
        } else {
            o.put("customer", JSONObject.NULL);
        }
        JSONArray lines = new JSONArray();
        for (TicketLine l : this.lines) {
            lines.put(l.toJSON(this.area));
            if (l.getProduct() instanceof CompositionInstance) {
                // Add content lines for stock and sales
                CompositionInstance inst = (CompositionInstance) l.getProduct();
                for (Product p : inst.getProducts()) {
                    Product sub = new Product(p.getId(), p.getLabel(), 0.0,
                            p.getTaxId(), p.getTaxRate(), p.isScaled());
                    TicketLine subLine = new TicketLine(sub, 1);
                    lines.put(subLine.toJSON(null));
                }
            }
        }
        o.put("lines", lines);
        return o;
    }

    @Override
    public String toString() {
        return this.label + " (" + this.articles + " articles)";
    }
}
