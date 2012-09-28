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
import android.util.Log;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private static final String LOG_TAG = "POS-TECH/Data";

    public static boolean loadAll(Context ctx) {
        boolean ok = true;
        // Load session
        try {
            SessionData.loadSession(ctx);
            Log.i(LOG_TAG, "Local session loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No session file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading session", ioe);
            }
        }
        // Load receipts
        try {
            ReceiptData.load(ctx);
            Log.i(LOG_TAG, "Local receipts loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No receipts file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading session", ioe);
            }
        }
        // Load catalog
        try {
            ok &= CatalogData.load(ctx);
            Log.i(LOG_TAG, "Local catalog loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No catalog file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading catalog", ioe);
            }
            ok = false;
        }
        // Load users
        try {
            ok &= UserData.load(ctx);
            Log.i(LOG_TAG, "Local users loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No users file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading users", ioe);
            }
            ok = false;
        }
        // Load cash
        try {
            CashData.load(ctx);
            Log.i(LOG_TAG, "Local cash loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No cash file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading cash", ioe);
            }
        }
        // Load places
        try {
            PlaceData.load(ctx);
            Log.i(LOG_TAG, "Local places loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No places file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading places", ioe);
            }
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