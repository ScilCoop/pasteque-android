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

import fr.postech.client.models.Cash;
import fr.postech.client.models.Session;

/** Utility class to check and load local data */
public class DataLoader {

    public static boolean loadAll(Context ctx) {
        boolean ok = true;
        // Load session
        try {
            Session s = SessionData.loadSession(ctx);
            Session.currentSession = s;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Load receipts
        try {
            ReceiptData.load(ctx);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Load catalog
        try {
            ok &= CatalogData.load(ctx);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            ok = false;
        }
        // Load users
        try {
            ok &= UserData.load(ctx);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            ok = false;
        }
        // Load cash
        try {
            CashData.load(ctx);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ok;
    }

    public static boolean dataLoaded() {
        return UserData.users != null && UserData.users.size() > 0
            && CatalogData.catalog != null
            && CatalogData.catalog.getRootCategories().size() > 0;
    }

    public static boolean hasDataToSend() {
        return (ReceiptData.getReceipts() != null
                && ReceiptData.getReceipts().size() > 0)
            || CashData.dirty == true;
    }
}