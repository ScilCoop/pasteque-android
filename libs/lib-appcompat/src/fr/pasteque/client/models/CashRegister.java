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

public class CashRegister implements Serializable {

    private int id;
    private String machineName;
    private String locationId;
    private int nextTicketId;

    public CashRegister (int id, String label, String locationId,
            int nextTicketId) {
        this.id = id;
        this.machineName = label;
        this.locationId = locationId;
        this.nextTicketId = nextTicketId;
    }

    public int getId() {
        return this.id;
    }

    public String getMachineName() {
        return this.machineName;
    }

    public String getLocationId() {
        return this.locationId;
    }

    public int getNextTicketId() {
        return this.nextTicketId;
    }

    public static CashRegister fromJSON(JSONObject o) throws JSONException {
        int id = o.getInt("id");
        String name = o.getString("label");
        String locationId = o.getString("locationId");
        int nextTicketId = o.getInt("nextTicketId");
        return new CashRegister(id, name, locationId, nextTicketId);
    }

}