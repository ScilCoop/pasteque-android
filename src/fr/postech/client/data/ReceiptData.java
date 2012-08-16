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
package fr.postech.client.data;

import fr.postech.client.models.Receipt;

import android.content.Context;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Stores finalized tickets */
public class ReceiptData {

    private static final String FILENAME = "tickets.data";

    private static List<Receipt> receipts = new ArrayList<Receipt>();

    public static void addReceipt(Receipt r) {
        receipts.add(r);
    }

    public static List<Receipt> getReceipts() {
        return receipts;
    }

    public static boolean save(Context ctx)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(FILENAME, ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(receipts);
        oos.close();
        return true;
    }

    public static void load(Context ctx)
        throws IOException {
        FileInputStream fis = ctx.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            receipts = (List) ois.readObject();
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
    }
    
    public static JSONArray toJSON() throws JSONException {
        JSONArray array = new JSONArray();
        for (Receipt r : receipts) {
            array.put(r.toJSON());
        }
        return array;
    }
}