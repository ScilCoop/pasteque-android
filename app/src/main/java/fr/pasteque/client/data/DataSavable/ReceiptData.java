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
package fr.pasteque.client.data.DataSavable;

import com.google.gson.reflect.TypeToken;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.Receipt;

import android.content.Context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

/** Stores finalized tickets */
public class ReceiptData extends AbstractJsonDataSavable {

    private static final String FILENAME = "tickets.json";

    private List<Receipt> receipts = new ArrayList<>();

    @Override
    protected String getFileName() {
        return ReceiptData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(receipts);
        return result;
    }

    @Override
    protected List<Type> getClassList() {
        List<Type> result = new ArrayList<>();
        result.add(new TypeToken<List<Receipt>>(){}.getType());
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        this.receipts = (List<Receipt>) objs.get(0);
    }

    public void addReceipt(Receipt r) {
        receipts.add(r);
    }

    public List<Receipt> getReceipts(Context ctx) {
        if (receipts.size() == 0) {
            this.loadNoMatterWhat(ctx);
        }
        return receipts;
    }

    public boolean hasReceipts() {
        return receipts.size() > 0;
    }

    /** Delete current receipts and save */
    public void clear(Context ctx) {
        receipts.clear();
        save(Pasteque.getAppContext());
    }
    
    public JSONArray toJSON(Context ctx) throws JSONException {
        JSONArray array = new JSONArray();
        for (Receipt r : receipts) {
            array.put(r.toJSON());
        }
        return array;
    }
}
