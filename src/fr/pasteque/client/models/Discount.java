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
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author svirch_n
 */
public class Discount implements Serializable{

    private String id;
    private double rate;
    private double startDate;
    private double endDate;
    private String barcode;
    private int barcodeType;
    
    public Discount(String id, double rate, double start, double end, String barcode, int barcodeType)
    {
        this.id = id;
        this.rate = rate;
        this.startDate = start;
        this.endDate = end;
        this.barcode = barcode;
        this.barcodeType = barcodeType;
    }

    public String getId() {
        return id;
    }

    public double getRate() {
        return rate;
    }

    public double getStartDate() {
        return startDate;
    }

    public double getEndDate() {
        return endDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getBarcodeType() {
        return barcodeType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setStartDate(double startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(double endDate) {
        this.endDate = endDate;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setBarcodeType(int barcodeType) {
        this.barcodeType = barcodeType;
    }
    
    public static Discount fromJSON(JSONObject o)
        throws JSONException {
        String id = o.getString("id");
        double rate = o.getDouble("rate");
        double startDate = o.getDouble("startDate");
        double endDate = o.getDouble("endDate");
        String barcode = o.getString("barcode");
        int barcodeType = o.getInt("barcodeType");
        return new Discount(id, rate, startDate, endDate, barcode, barcodeType);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("rate", this.rate);
        o.put("startDate", this.startDate);
        o.put("endDate", this.endDate);
        o.put("barcode", this.barcode);
        o.put("barcodeType", this.barcodeType);
        return o;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Discount && ((Discount)o).id != null) {
            return ((Discount)o).id.equals(this.id);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Discount: " +  "(" + this.id + ":" + this.barcode + ":" + Barcode.toString(this.barcodeType) + ")";
    }
    
}
