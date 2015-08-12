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
import fr.pasteque.client.data.DataSavable.*;
import fr.pasteque.client.utils.exception.DataCorruptedException;

import java.io.IOError;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to check and load local data
 */
public class Data {

    private static final String LOG_TAG = "Pasteque/Data";

    public static CatalogData Catalog = new CatalogData();
    public static CashData Cash = new CashData();
    public static CashRegisterData CashRegister = new CashRegisterData();
    public static CompositionData Composition = new CompositionData();
    public static CrashData Crash = new CrashData();
    public static CustomerData Customer = new CustomerData();
    public static DiscountData Discount = new DiscountData();
    public static PaymentModeData PaymentMode = new PaymentModeData();
    public static PlaceData Place = new PlaceData();
    public static ReceiptData Receipt = new ReceiptData();
    public static SessionData Session = new SessionData();
    public static TariffAreaData TariffArea = new TariffAreaData();
    public static UserData User = new UserData();

    public static boolean loadAll(Context ctx) {
        boolean result = true;

        // List of DataSavable classes to load
        ArrayList<DataSavable> list = new ArrayList<>();
        list.add(Catalog);
        list.add(Cash);
        list.add(CashRegister);
        list.add(Composition);
        list.add(Crash);
        list.add(Customer);
        list.add(Discount);
        list.add(PaymentMode);
        list.add(Place);
        list.add(Receipt);
        list.add(Session);
        list.add(TariffArea);
        list.add(User);

        for (DataSavable data: list) {
            try {
                data.load(ctx);
                Log.i(LOG_TAG, "Correctly loaded: " + data.getClass().getName());
            } catch (DataCorruptedException e) {
                Log.d(LOG_TAG, "Warning: " + data.getClass().getName());
                Log.d(LOG_TAG, e.inspectError());
            } catch (IOError e) {
                result = false;
                Log.e(LOG_TAG, "Fatal IO Error: " + data.getClass().getName(), e);
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
                result = false;
            }
        }

        return result;
    }

    public static boolean dataLoaded(Context ctx) {
        return Data.User.users(ctx) != null && Data.User.users(ctx).size() > 0
                && Data.Catalog.catalog(ctx) != null
                && Data.Catalog.catalog(ctx).getRootCategories().size() > 0
                && Data.Catalog.catalog(ctx).getProductCount() > 0
                && Data.PaymentMode.paymentModes(ctx) != null
                && Data.PaymentMode.paymentModes(ctx).size() > 0
                && Data.Cash.currentCash(ctx) != null;
    }

    public static boolean hasCashOpened(Context ctx) {
        return (Data.Receipt.getReceipts(ctx).size() > 0)
                || Data.Cash.dirty;
    }

    public static boolean hasArchive(Context ctx) {
        return CashArchive.hasArchives(ctx);
    }

    public static boolean hasLocalData(Context ctx) {
        return Data.hasCashOpened(ctx) || Data.hasArchive(ctx);
    }
}
