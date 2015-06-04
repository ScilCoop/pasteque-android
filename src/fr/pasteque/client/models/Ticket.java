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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.TariffAreaData;

public class Ticket implements Serializable {

    private String id;
    private String label;
    private int articles;
    private List<TicketLine> lines;
    private Customer customer;
    private TariffArea area;
    private User user;
    private Integer discountProfileId;
    private double discountRate;
    private Integer custCount;

    private static final String LOGTAG = "Tickets";
    private static final String JSONERR_AREA = "Error while parsing Area JSON, setting Area to null";
    private static final String JSONERR_CUSTOMER = "Error while parsing Costumer JSON, setting Costumer to null";

    public Ticket() {
        this.id = UUID.randomUUID().toString();
        this.lines = new ArrayList<TicketLine>();
    }

    public Ticket(String label) {
        this.id = UUID.randomUUID().toString();
        this.label = label;
        this.lines = new ArrayList<TicketLine>();
    }

    public Ticket(String id, String label) {
        this.id = id;
        this.lines = new ArrayList<TicketLine>();
        this.label = label;
    }

    public String getId() {
        return this.id;
    }

    public TariffArea getTariffArea() {
        return this.area;
    }

    public User getUser() {
        return this.user;
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
            // Removes only 1 article for a scaled product
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
     * @return false if line is removed, true otherwise
     */
    public boolean adjustScale(TicketLine l, double scale) {
        if (scale > 0) {
            for (TicketLine li : this.lines) {
                if (li.equals(l)) {
                    l.setQuantity(scale);
                    return true;
                }
            }
        }
        this.removeLine(l);
        return false;
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
            total += l.getSubtotalPrice(this.area);
        }
        return total;
    }

    public double getTaxPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTaxPrice(this.area);
        }
        return total;
    }

    public Map<Double, Double> getTaxes() {
        Map<Double, Double> taxes = new HashMap<Double, Double>();
        for (TicketLine l : this.lines) {
            double rate = l.getProduct().getTaxRate();
            double amount = l.getTaxPrice(this.area);
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
        if(c != null && !c.getTariffAreaId().equals("0")) {
            List<TariffArea> tariffAreasList = new ArrayList<TariffArea>();
            tariffAreasList.addAll(TariffAreaData.areas);
            for (TariffArea tariffArea : tariffAreasList) {
                if(tariffArea.getId().equals(c.getTariffAreaId())) {
                    this.setTariffArea(tariffArea);
                    break;
                }
            }
        }
    }

    // Boolean is here in order to manage ID which has to be serialized or not
    public JSONObject toJSON(boolean isShared) throws JSONException {
        JSONObject o = new JSONObject();

        if (isShared) {
            if (this.id != null) {
                o.put("id", id);
            } else {
                o.put("id", JSONObject.NULL);
            }
        } else {
            o.put("type", 0);
        }
        if (this.label != null) {
            o.put("label", label);
        } else {
            o.put("label", JSONObject.NULL);
        }

        if (this.customer != null) {
            o.put("customerId", this.customer.getId());
        } else {
            o.put("customerId", JSONObject.NULL);
        }
        if (this.custCount != null) {
            o.put("custCount", this.custCount);
        } else {
            o.put("custCount", JSONObject.NULL);
        }
        if (this.area != null) {
            o.put("tariffAreaId", this.area.getId());
        } else {
            o.put("tariffAreaId", JSONObject.NULL);
        }

        if (this.discountProfileId != null) {
            o.put("discountProfileId", this.discountProfileId);
        } else {
            o.put("discountProfileId", JSONObject.NULL);
        }
        o.put("discountRate", this.discountRate);

        JSONArray lines = new JSONArray();
        int i = 0;
        for (TicketLine l : this.lines) {
            JSONObject line = l.toJSON(this.id, area);
            line.put("dispOrder", i);
            lines.put(line);
            i++;
            if (l.getProduct() instanceof CompositionInstance) {
                // Add content lines for stock and sales
                CompositionInstance inst = (CompositionInstance) l.getProduct();
                for (Product p : inst.getProducts()) {
                    Product sub = new Product(p.getId(), p.getLabel(), null,
                            0.0, p.getTaxId(), p.getTaxRate(), p.isScaled(),
                            p.hasImage());
                    TicketLine subTktLine = new TicketLine(sub, 1);
                    JSONObject subline = subTktLine.toJSON(isShared ? this.id : null, area);
                    subline.put("dispOrder", i);
                    i++;
                    lines.put(subline);
                }
            }
        }
        o.put("lines", lines);
        return o;
    }

    public static Ticket fromJSON(Context context, JSONObject o)
            throws JSONException {
        Ticket result = new Ticket(o.getString("id"), o.getString("label"));
        if (!o.isNull("custCount")) {
            result.custCount = o.getInt("custCount");
        }
        // Getting Tarif area
        try {
            List<TariffArea> areas = TariffAreaData.areas;
            if (!o.isNull("tariffAreaId")) {
                String tarifAreaId = Integer.toString(o.getInt("tariffAreaId"));
                for (int i = 0; i < areas.size(); ++i) {
                    if (areas.get(i).getId().equals(tarifAreaId) == true) {
                        result.area = areas.get(i);
                        break;
                    }
                }
            } else {
                result.area = null;
            }
        } catch (JSONException e) {
            Log.e(LOGTAG, JSONERR_AREA);
            result.area = null;
        }

        // Getting Customer
        try {
            List<Customer> customers = CustomerData.customers;
            String customerId = o.getString("customerId");
            for (int i = 0; i < customers.size(); ++i) {
                if (customers.get(i).getId().equals(customerId) == true) {
                    result.customer = customers.get(i);
                    break;
                }
            }
        } catch (JSONException e) {
            Log.e(LOGTAG, JSONERR_CUSTOMER);
            result.customer = null;
        }

        if (!o.isNull("discountProfileId")) {
            result.discountProfileId = o.getInt("discountProfileId");
        }
        result.discountRate = o.getDouble("discountRate");

        // Getting all lines
        JSONArray array = o.getJSONArray("lines");
        result.articles = 0;
        for (int i = 0; i < array.length(); ++i) {
            JSONObject current = array.getJSONObject(i);
            TicketLine currentLine = TicketLine.fromJSON(context, current);
            result.articles += currentLine.getQuantity();
            result.lines.add(currentLine);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.label + " (" + this.articles + " articles)";
    }
}
