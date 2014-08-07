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
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class Cash implements Serializable {

    private String id;
    private int cashRegisterId;
    private int sequence;
    private long openDate;
    private long closeDate;

    /** Create an empty cash */
    public Cash(int cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
        this.openDate = -1;
        this.closeDate = -1;
    }
    
    public Cash(String id, int cashRegisterId, int sequence, long openDate,
            long closeDate) {
        this.id = id;
        this.cashRegisterId = cashRegisterId;
        this.sequence = sequence;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }
   
    public String getId() {
        return this.id;
    }

    public int getCashRegisterId() {
        return this.cashRegisterId;
    }

    /** Get cash sequence. Returns 0 if it is not set. */
    public int getSequence() {
        return this.sequence;
    }
    
    public long getOpenDate() {
        return this.openDate;
    }

    /** Cash is opened when usable (opened and not closed) */
    public boolean isOpened() {
        return this.openDate != -1 && !this.isClosed();
    }

    public boolean wasOpened() {
        return this.openDate != -1;
    }

    public boolean isClosed() {
        return this.closeDate != -1;
    }

    public long getCloseDate() {
        return this.closeDate;
    }

    public void openNow() {
        this.openDate = System.currentTimeMillis() / 1000;
    }

    public void closeNow() {
        this.closeDate = System.currentTimeMillis() / 1000;
    }

    public boolean equals(Object o) {
        if (this.id == null) {
            return ((Cash)o).id == null
                    && ((Cash)o).machineName.equals(machineName);
        } else {
            return this.id.equals(((Cash)o).id);
        }
    }

    public static Cash fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        int cashRegId = o.getInt("cashRegisterId");
        long openDate = -1;
        if (o.has("openDate") && !o.isNull("openDate")) {
            openDate = o.getLong("openDate");
        }
        long closeDate = -1;
        if (o.has("closeDate") && !o.isNull("closeDate")) {
            closeDate = o.getLong("closeDate");
        }
        int sequence = o.getInt("sequence");
        return new Cash(id, cashRegId, sequence, openDate, closeDate);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.getId());
        o.put("cashRegisterId", this.getCashRegisterId());
        o.put("sequence", this.sequence);
        o.put("openDate", this.getOpenDate());
        if (this.isClosed()) {
            o.put("closeDate", this.getCloseDate());
        }
        o.put("openCash", JSONObject.NULL);
        o.put("closeCash", JSONObject.NULL);
        o.put("expectedCash", JSONObject.NULL);
        return o;
    }

    @Override
    public String toString() {
        return "(" + this.id + ", "
            + this.openDate + "-" + this.closeDate + ")";
    }
}