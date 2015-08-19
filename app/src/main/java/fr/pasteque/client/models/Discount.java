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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.pasteque.client.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author svirch_n
 */
public class Discount implements Serializable{

    public final static int DEFAULT_DISCOUNT_RATE = 0;
    private final static String LOG_TAG = "pasteque/discount";
    
    
    private String id;
    private double rate;
    private Date startDate;
    private Date endDate;
    private Barcode barcode;
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private String label;

    public Discount(String id, String label, double rate, String start, String end, String barcode, int barcodeType) throws ParseException {
        this(id, rate,start,end,barcode,barcodeType);
        this.label = label;
    }

    public Discount(String id, double rate, String start, String end, String barcode, int barcodeType) throws ParseException
    {
        this.id = id;
        this.rate = rate;
        if (barcode == null) {
            Log.w(LOG_TAG, "barcode null not expected");
        }
        this.startDate = convertDateFromString(start);
        this.endDate = convertDateFromString(end);
        this.barcode = new Barcode(barcode, barcodeType);
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

    public Barcode getBarcode() {
        return barcode;
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

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) throws ParseException {
        this.endDate = convertDateFromString(endDate);
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setBarcode(String barcode) {
        this.barcode.setCode(barcode);
    }

    public void setBarcodeType(int barcodeType) {
        this.barcode.setType(barcodeType);
    }
    
    public static Date convertDateFromString(String dateInString) throws ParseException {
        return Discount.formatter.parse(dateInString);            
    }

    public static String convertDateToString(Date date) throws ParseException {
        return Discount.formatter.format(date);            
    }
    
    public static Discount fromJSON(JSONObject o)
        throws JSONException, ParseException {
        String id = o.getString("id");
        String label = o.getString("label");
        double rate = o.getDouble("rate");
        String startDate = o.getString("startDate");
        String endDate = o.getString("endDate");
        String barcode = o.getString("barcode");
        int barcodeType = o.getInt("barcodeType");
        return new Discount(id, label, rate, startDate, endDate, barcode, barcodeType);
    }
    
    public JSONObject toJSON() throws JSONException, ParseException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("label", this.label);
        o.put("rate", this.rate);
        o.put("startDate", convertDateToString(this.startDate));
        o.put("endDate", convertDateToString(this.endDate));
        o.put("barcode", this.barcode.getCode());
        o.put("barcodeType", this.barcode.getType());
        return o;
    }
    
    @Override
    public boolean equals(Object o) {
        //noinspection SimplifiableIfStatement
        if (o instanceof Discount && ((Discount)o).id != null) {
            return ((Discount)o).id.equals(this.id);
        }
        return false;
    }

    public String getTitle(Context ctx) {
        return ctx.getResources().getString(R.string.barcode_message_title) + " " + this.rate * 100 + "%" ;
    }

    public String getDate(Context ctx) {
        try {
            return ctx.getString(R.string.barcode_message_date,
                    convertDateToString(this.getStartDate()),
                    convertDateToString(this.getEndDate()));
        } catch (ParseException e) {
            return "Date Error!";
        }
    }

    public boolean isValid() {
        if (endDate.before(startDate))
            Log.w(LOG_TAG, "Corrupted Discount, endDate is anterior to startDate");
        Date now = Calendar.getInstance().getTime();
        return now.after(startDate) && now.before(endDate);
    }
}
