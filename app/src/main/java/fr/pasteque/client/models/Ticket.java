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
import java.text.SimpleDateFormat;
import java.util.*;

import fr.pasteque.client.Configure;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.sync.TicketUpdater;
import fr.pasteque.client.utils.CalculPrice;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

/**
 * Ticket is Shareable
 * It means that any edited required informations for the server are updated if configured to do so
 * required information are edited in _methods
 * make sure to keep this semantic and behaviors.
 */
public class Ticket implements Serializable {

    private static final String DATE_FORMAT = "HH:mm:ss";
    private String id; //
    private String ticketId;
    private int articles;
    private List<TicketLine> lines;
    private Customer customer;
    private TariffArea area;
    private User user;
    private Integer discountProfileId;
    private double discountRate;
    private Integer custCount;
    private long serverDate_seconds;

    // TicketId is only set on payment action
    // The equivalent of ticketId is the creationTime
    private double creationTime;

    private static final String LOGTAG = "Tickets";
    private static final String JSONERR_AREA = "Error while parsing Area JSON, setting Area to null";
    private static final String JSONERR_CUSTOMER = "Error while parsing Costumer JSON, setting Costumer to null";
    private static final String JSONERR_DATE = "Error parsing date in JSON, setting it to 0";
    private String label = null;

    private void _init(String id, String label) {
        this.id = id;
        this.ticketId = label;
        this.label = label;
        this.lines = new ArrayList<TicketLine>();
        this.creationTime = new Date().getTime();
    }

    private void _addTicketLine(TicketLine ticketLine) {
        this.lines.add(ticketLine);
        this.updateTicket();
    }

    private void _removeTicketLine(TicketLine ticketLine) {
        this.lines.remove(ticketLine);
        this.updateTicket();
    }

    private void _adjustTicketLine(TicketLine ticketLine, int qtt) {
        ticketLine.adjustQuantity(qtt);
        this.updateTicket();
    }

    private void _setTicketLineQtt(TicketLine ticketLine, double qtt) {
        ticketLine.setQuantity(qtt);
        this.updateTicket();
    }

    private void _setCustomer(Customer c) {
        this.customer = c;
        this.updateTicket();
    }

    private void updateTicket() {
        Context context = Pasteque.getAppContext();
        if (Configure.getSyncMode(context) == Configure.AUTO_SYNC_MODE) {
            TicketUpdater.getInstance().execute(context,
                    null,
                    TicketUpdater.TICKETSERVICE_SEND
                            | TicketUpdater.TICKETSERVICE_ONE, this);
        }
    }

    public Ticket() {
        _init(UUID.randomUUID().toString(), null);
    }

    public Ticket(String label) {
        _init(UUID.randomUUID().toString(), label);
    }

    public Ticket(String id, String label) {
        _init(id, label);
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public void setDiscountRate(double rate) {
        this.discountRate = rate;
    }

    public double getDiscountRate() {
        return this.discountRate;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        if (this.label != null) {
            return label;
        }
        return new SimpleDateFormat(DATE_FORMAT).format(creationTime);
    }

    public TariffArea getTariffArea() {
        return this.area;
    }

    public User getUser() {
        return this.user;
    }

    public long getServerDateInSeconds() {
        return this.serverDate_seconds;
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
        _addTicketLine(new TicketLine(p, qty, getTariffArea()));
        this.articles += Math.abs(qty);
    }

    /**
     * Adds a line with a scaled product
     *
     * @param p     the product to add
     * @param scale the product's weight
     */
    public void addLineProductScaled(Product p, double scale) {
        _addTicketLine(new TicketLine(p, scale, getTariffArea()));
        this.articles += 1;
    }

    public void removeLine(TicketLine l) {
        _removeTicketLine(l);
        // Removes only 1 article for a scaled product
        if (l.getProduct().isScaled()) {
            this.articles--;
        } else {
            this.articles -= l.getArticlesNumber();
        }
    }

    /**
     * Add product to ticket
     *
     * @param p is the product to add
     * @return product's position
     */
    public int addProduct(Product p) {
        int position = 0;
        for (TicketLine l : this.lines) {
            if (l.getProduct().equals(p) && !l.isCustom()) {
                this.adjustProductQuantity(l, 1);
                return position;
            }
            position++;
        }
        this.addLine(p, 1);
        return position;
    }

    public int addProductReturn(Product p) {
        int position = 0;
        for (TicketLine l : this.lines) {
            if (l.getProduct().equals(p) && !l.isCustom()) {
                this.adjustProductQuantity(l, -1);
                return position;
            }
            position++;
        }
        this.addLine(p, -1);
        return position;
    }

    public void addScaledProductReturn(Product p, double scale) {
        this.addLineProductScaled(p, -scale);
    }

    /**
     * Adds scaled product to the ticket
     *
     * @param p     the product to add
     * @param scale the products weight
     */
    public void addScaledProduct(Product p, double scale) {
        this.addLineProductScaled(p, scale);
    }

    private boolean adjustProductQuantity(TicketLine l, int qty) {
        if (this.sameSign(l.getQuantity() + qty, l.getQuantity())) { //if the quantity's numeric sign didn't change
            _adjustTicketLine(l, qty);
            if (this.sameSign(l.getQuantity(), qty)) {
                this.articles += Math.abs(qty);
            } else {
                this.articles -= Math.abs(qty);
            }
            return true;
        }
        this.removeLine(l);
        return false;
    }

    private boolean sameSign(double quantity, double qty) {
        return quantity * qty > 0;
    }

    /**
     * Add/subtract quantity to non-scaled product and removes it if final qty < 0
     *
     * @param l   is the ticket line to edit
     * @param qty is the quantity to add
     * @return false if line is remove, true otherwise
     */
    public boolean adjustQuantity(TicketLine l, int qty) {
        return this.adjustProductQuantity(l, qty);
    }

    /**
     * Adjusts the weight of a scaled product
     *
     * @param l     the ticket's line of the product to modify
     * @param scale the modify weight
     * @return false if line is removed, true otherwise
     */
    public boolean adjustScale(TicketLine l, double scale) {
        if (scale > 0) {
            _setTicketLineQtt(l, scale);
            return true;
        }
        this.removeLine(l);
        return false;
    }

    public int getArticlesCount() {
        return this.articles;
    }

    public double getTicketPrice() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTotalDiscIncTax(this.discountRate);
        }
        return total;
    }

    public double getTicketPriceExcTax() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTotalDiscExcTax(this.discountRate);
        }
        return total;
    }

    public double getTaxCost() {
        double total = 0.0;
        for (TicketLine l : this.lines) {
            total += l.getTotalTaxCost(this.discountRate);
        }
        return total;
    }

    public Map<Double, Double> getTaxes() {
        Map<Double, Double> taxes = new HashMap<Double, Double>();
        for (TicketLine l : this.lines) {
            double rate = l.getProduct().getTaxRate();
            double amount = l.getTotalTaxCost(this.discountRate);
            if (taxes.containsKey(rate)) {
                amount += taxes.get(rate);
            }
            taxes.put(rate, amount);
        }
        return taxes;
    }

    public String getTicketId() {
        return this.ticketId;
    }

    public boolean isEmpty() {
        return this.lines.size() == 0;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer c) {
        _setCustomer(c);
        if (c != null && !c.getTariffAreaId().equals("0")) {
            List<TariffArea> tariffAreasList = new ArrayList<TariffArea>();
            tariffAreasList.addAll(Data.TariffArea.areas);
            for (TariffArea tariffArea : tariffAreasList) {
                if (tariffArea.getId().equals(c.getTariffAreaId())) {
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
        if (this.ticketId != null) {
            o.put("ticketId", ticketId);
        } else {
            o.put("ticketId", JSONObject.NULL);
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
        o.put("label", this.getLabel());

        JSONArray lines = new JSONArray();
        int i = 0;
        for (TicketLine l : this.lines) {
            JSONObject line = l.toJSON();
            line.put("dispOrder", i);
            lines.put(line);
            i++;
            if (l.getProduct() instanceof CompositionInstance) {
                // Add content lines for stock and sales
                CompositionInstance inst = (CompositionInstance) l.getProduct();
                for (Product p : inst.getProducts()) {
                    Product sub = new Product(p.getId(), p.getLabel(), null,
                            0.0, p.getTaxId(), p.getTaxRate(), p.isScaled(),
                            p.hasImage(), p.getDiscountRate(), p.isDiscountRateEnabled());
                    TicketLine subTktLine = new TicketLine(sub, 1, getTariffArea());
                    JSONObject subline = subTktLine.toJSON();
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
        String id = o.getString("id");
        String label = o.getString("label");
        Ticket result = new Ticket(id, label);
        if (!o.isNull("custCount")) {
            result.custCount = o.getInt("custCount");
        }
        // Getting server date
        try {
            result.serverDate_seconds = 0;
            if (o.has("date")) {
                result.serverDate_seconds = o.getLong("date");
            }
        } catch (JSONException e) {
            Log.e(LOGTAG, JSONERR_DATE);
        }
        // Getting Tarif area
        try {
            List<TariffArea> areas = Data.TariffArea.areas;
            if (!o.isNull("tariffAreaId")) {
                String tarifAreaId = Integer.toString(o.getInt("tariffAreaId"));
                for (int i = 0; i < areas.size(); ++i) {
                    if (tarifAreaId.equals(areas.get(i).getId())) {
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
            List<Customer> customers = Data.Customer.customers;
            String customerId = o.getString("customerId");
            for (int i = 0; i < customers.size(); ++i) {
                if (customers.get(i).getId().equals(customerId)) {
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
            TicketLine currentLine = TicketLine.fromJSON(context, current, result.area);
            result.articles += currentLine.getQuantity();
            result.lines.add(currentLine);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.ticketId + " (" + this.articles + " articles)";
    }

    public String getDiscountRateString() {
        double pourcent = this.discountRate * 100;
        return ((int) pourcent) + " %";
    }

    public double getFinalDiscount() {
        double result = 0;
        for (TicketLine l : this.lines) {
            result += l.getTotalDiscPIncTax();
        }
        return CalculPrice.getDiscountCost(result, this.discountRate);
    }

    public double getGenericPrice(Product p, int binaryMask) {
        return p.getGenericPrice(this.area, binaryMask);
    }
}
