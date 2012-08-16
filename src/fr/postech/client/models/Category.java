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

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class Category implements Serializable {

    private String id;
    private String label;
    private List<Category> subcategories;

    public Category(String id, String label) {
        this.id = id;
        this.label = label;
        this.subcategories = new ArrayList<Category>();
    }

    public String getLabel() {
        return this.label;
    }

    public String getId() {
        return this.id;
    }

    public Drawable getIcon() {
        return null;
    }

    public List<Category> getSubcategories() {
        return this.subcategories;
    }

    public void addSubcategory(Category c) {
        this.subcategories.add(c);
    }

    public boolean equals(Object o ) {
        return o instanceof Category && ((Category) o).id.equals(this.id);
    }

    public static Category fromJSON(JSONObject o) throws JSONException {
        String label = o.getString("label");
        String id = o.getString("id");
        return new Category(id, label);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("label", this.label);
        o.put("id", this.id);
        return o;
    }
}
