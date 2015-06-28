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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private Date startDate;
    private Date endDate;
    private String barcode;
    private int barcodeType;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    
    public Discount(String id, double rate, String start, String end, String barcode, int barcodeType) throws ParseException
    {
        this.id = id;
        this.rate = rate;
        this.startDate = convertDateFromString(start);
        this.endDate = convertDateFromString(end);
        this.barcode = barcode;
        this.barcodeType = barcodeType;
    }

    public String getId() {
        return id;
    }

    public double getRate() {
        return rate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
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

    public void setStartDate(String startDate) throws ParseException {
        this.startDate = convertDateFromString(startDate);
    }

    public void setEndDate(String endDate) throws ParseException {
        this.endDate = convertDateFromString(endDate);
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setBarcodeType(int barcodeType) {
        this.barcodeType = barcodeType;
    }
    
    private Date convertDateFromString(String dateInString) throws ParseException {
        return formatter.parse(dateInString);            
    }
    
    private String convertDateToString(Date date) throws ParseException {
        return formatter.format(date);            
    }
    
    public static Discount fromJSON(JSONObject o)
        throws JSONException, ParseException {
        String id = o.getString("id");
        double rate = o.getDouble("rate");
        String startDate = o.getString("startDate");
        String endDate = o.getString("endDate");
        String barcode = o.getString("barcode");
        int barcodeType = o.getInt("barcodeType");
        return new Discount(id, rate, startDate, endDate, barcode, barcodeType);
    }
    
    public JSONObject toJSON() throws JSONException, ParseException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("rate", this.rate);
        o.put("startDate", convertDateToString(this.startDate));
        o.put("endDate", convertDateToString(this.endDate));
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
        return this.id + ":" + this.barcode + ":" + Barcode.toString(this.barcodeType);
    }
    
}
