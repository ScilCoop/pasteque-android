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
package fr.pasteque.client.data;

import fr.pasteque.client.models.Stock;

import android.content.Context;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StockData {

    private static final String FILENAME = "stock.data";
    private static final String LOC_FILENAME = "location.data";

    public static Map<String, Stock> stocks;

    public static boolean save(Context ctx)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(FILENAME, ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(stocks);
        oos.close();
        return true;
    }

    public static boolean load(Context ctx)
        throws IOException {
        boolean ok = false;
        FileInputStream fis = ctx.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            stocks = (Map<String, Stock>) ois.readObject();
            if (stocks.keySet().size() > 0) {
                ok = true;
            }
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
        return ok;
    }

    public static boolean saveLocation(Context ctx, String location, String id)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(LOC_FILENAME,
                ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(location);
        oos.writeObject(id);
        oos.close();
        return true;
    }

    /** Get location id. Return null if not found or not requested location. */
    public static String getLocationId(Context ctx, String location)
        throws IOException {
        FileInputStream fis = ctx.openFileInput(LOC_FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        String id = null;
        try {
            String loc = (String) ois.readObject();
            if (loc.equals(location)) {
                id = (String) ois.readObject();
            }
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
        return id;
    }
}
