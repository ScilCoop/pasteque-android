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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.interfaces.Item;
import org.json.JSONException;
import org.json.JSONObject;

public class Category implements Serializable, Item {

    private String id;
    private String label;
    private List<Category> subcategories;
    private boolean hasImage;

    public Category(String id, String label, boolean hasImage) {
        this.id = id;
        this.label = label;
        this.hasImage = hasImage;
        this.subcategories = new ArrayList<Category>();
    }

    @Override
    public Type getType() {
        return Type.Category;
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

    public boolean hasImage() {
        return this.hasImage;
    }

    @Override
    public Bitmap getImage(Context ctx) {
        return ImagesData.getCategoryImage(this.getId());
    }

    public List<Category> getSubcategories() {
        return this.subcategories;
    }

    public void addSubcategory(Category c) {
        this.subcategories.add(c);
    }

    @Override
	public boolean equals(Object o ) {
        return o instanceof Category && ((Category) o).id.equals(this.id);
    }

    public static Category fromJSON(JSONObject o) throws JSONException {
        String label = o.getString("label");
        String id = o.getString("id");
        boolean hasImage = o.getBoolean("hasImage");
        return new Category(id, label, hasImage);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("label", this.label);
        o.put("id", this.id);
        return o;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (subcategories != null ? subcategories.hashCode() : 0);
        result = 31 * result + (hasImage ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", subcategories=" + subcategories +
                ", hasImage=" + hasImage +
                '}';
    }
}
