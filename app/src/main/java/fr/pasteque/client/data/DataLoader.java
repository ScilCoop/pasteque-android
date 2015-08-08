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

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.FileNotFoundException;

/** Utility class to check and load local data */
public class DataLoader {

    private static final String LOG_TAG = "Pasteque/Data";

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
                ok = false;
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
                Log.e(LOG_TAG, "Error while loading receipts", ioe);
                ok = false;
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
                ok = false;
            }
        }
        // Load compositions
        try {
            ok &= CompositionData.load(ctx);
            Log.i(LOG_TAG, "Local compositions loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No compositions file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading compositions", ioe);
                ok = false;
            }
        }
        // Load tariff areas
        try {
            ok &= TariffAreaData.load(ctx);
            Log.i(LOG_TAG, "Local tariff areas loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No tariff areas file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading tariff areas", ioe);
                ok = false;
            }
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
                ok = false;
            }
        }
        // Load customers
        try {
            ok &= CustomerData.load(ctx);
            Log.i(LOG_TAG, "Local customers loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No customers file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading customers", ioe);
                ok = false;
            }
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
                ok = false;
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
                ok = false;
            }
        }
        // Load stocks
        try {
            StockData.load(ctx);
            Log.i(LOG_TAG, "Stocks loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No stocks file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading stocks", ioe);
                ok = false;
            }
        }
        // Load payment modes
        try {
            PaymentModeData.load(ctx);
            Log.i(LOG_TAG, "Payment modes loaded");
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                Log.i(LOG_TAG, "No payment modes file to load");
            } else {
                Log.e(LOG_TAG, "Error while loading payment modes", ioe);
                ok = false;
            }
        }
        
        // One more load in this dynamic function, Discounts!
        try {
            DiscountData.load(ctx);
            Log.i(LOG_TAG, "Discount loaded");
        } catch (FileNotFoundException e) {
            Log.i(LOG_TAG, "No discount file to load");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while loading discounts", e);
            ok = false;
        }
        
        return ok;
    }

    public static boolean dataLoaded(Context ctx) {
        return UserData.users(ctx) != null && UserData.users(ctx).size() > 0
            && CatalogData.catalog(ctx) != null
            && CatalogData.catalog(ctx).getRootCategories().size() > 0
            && CatalogData.catalog(ctx).getProductCount() > 0
            && PaymentModeData.paymentModes(ctx) != null
            && PaymentModeData.paymentModes(ctx).size() > 0
            && CashData.currentCash(ctx) != null;
    }

    public static boolean hasCashOpened(Context ctx) {
        return (ReceiptData.getReceipts(ctx).size() > 0)
                || CashData.dirty;
    }

    public static boolean hasArchive(Context ctx) {
        return CashArchive.hasArchives(ctx);
    }

    public static boolean hasLocalData(Context ctx) {
        return DataLoader.hasCashOpened(ctx) || DataLoader.hasArchive(ctx);
    }
}
