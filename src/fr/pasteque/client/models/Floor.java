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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Floor implements Serializable {

    private String id;
    private String name;
    private List<Place> places;

    public Floor(String id, String name, List<Place> places) {
        this.id = id;
        this.name = name;
        this.places = places;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public static Floor fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        String name = o.getString("name");
        List<Place> places = new ArrayList<Place>();
        JSONArray jplaces = o.getJSONArray("places");
        for (int i = 0; i < jplaces.length(); i++) {
            JSONObject jplace = jplaces.getJSONObject(i);
            Place p = Place.fromJSON(jplace);
            places.add(p);
        }
        return new Floor(id, name, places);
    }
}