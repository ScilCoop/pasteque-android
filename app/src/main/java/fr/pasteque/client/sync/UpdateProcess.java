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
package fr.pasteque.client.sync;

import fr.pasteque.client.Configure;
import fr.pasteque.client.R;
import fr.pasteque.client.data.*;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.data.DataSavable.StockData;
import fr.pasteque.client.models.*;
import fr.pasteque.client.utils.Error;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.widgets.ProgressPopup;

import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager for update processus and UI feedback
 */
public class UpdateProcess implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/UpdateProcess";

    private static final int PHASE_DATA = 1;
    private static final int PHASE_IMG = 2;
    private static final int CTX_POOL_SIZE = 10;

    private static UpdateProcess instance;

    private Context ctx;
    private ProgressPopup feedback;
    private TrackedActivity caller;
    private Handler listener;
    private int progress;
    private int phase;
    // States for img phase
    private int openCtxCount;
    private List<Product> productsToLoad;
    private List<Category> categoriesToLoad;
    private List<PaymentMode> paymentModesToLoad;
    private int nextCtxIdx;
    private ImgUpdate imgUpdate;
    /**
     * True when something got wrong during sync, prevents running imgPhase
     */
    private boolean failed;

    private UpdateProcess(Context ctx) {
        this.ctx = ctx;
        this.phase = PHASE_DATA;
    }

    /**
     * Start update process with the given context (should be application
     * context). If already started nothing happens.
     *
     * @return True if started, false if already started.
     */
    public static boolean start(Context ctx) {
        if (instance == null) {
            // Create new process and run
            instance = new UpdateProcess(ctx);
            instance.failed = false;
            SyncUpdate syncUpdate = new SyncUpdate(instance.ctx,
                    new Handler(instance));
            syncUpdate.startSyncUpdate();
            return true;
        } else {
            // Already started
            return false;
        }
    }

    private void storeAccount() {
        Data.Login.setLogin(new Login(
                Configure.getUser(this.ctx),
                Configure.getPassword(this.ctx),
                Configure.getMachineName(this.ctx)));
        try {
            Data.Login.save(this.ctx);
        } catch (IOError e) {
            Log.e(LOG_TAG, "Could not save Login", e);
        }
    }

    private void invalidateAccount() {
        Data.Login.setLogin(new Login());
    }

    private void runImgPhase() {
        this.progress = 0;
        this.productsToLoad = new ArrayList<Product>();
        this.categoriesToLoad = new ArrayList<Category>();
        this.paymentModesToLoad = new ArrayList<PaymentMode>();
        Catalog c = Data.Catalog.catalog(this.ctx);
        for (Category cat : c.getAllCategories()) {
            if (cat.hasImage()) {
                this.categoriesToLoad.add(cat);
            }
            for (Product p : c.getProducts(cat)) {
                if (p.hasImage()) {
                    this.productsToLoad.add(p);
                }
            }
        }
        List<PaymentMode> modes = Data.PaymentMode.paymentModes(this.ctx);
        for (PaymentMode mode : modes) {
            if (mode.hasImage()) {
                this.paymentModesToLoad.add(mode);
            }
        }
        this.nextCtxIdx = 0;
        this.imgUpdate = new ImgUpdate(this.ctx, new Handler(this));
        if (this.feedback != null) {
            this.feedback.setProgress(0);
            this.feedback.setMax(this.productsToLoad.size()
                    + this.categoriesToLoad.size());
            this.feedback.setTitle(instance.ctx.getString(R.string.sync_img_title));
            this.feedback.setMessage(instance.ctx.getString(R.string.sync_img_message));
        }
        this.pool();
    }

    public static boolean isStarted() {
        return instance != null;
    }

    private void finish() {
        if (!this.failed) {
            this.storeAccount();
        } else {
            this.invalidateAccount();
        }
        Log.i(LOG_TAG, "Update sync finished.");
        SyncUtils.notifyListener(this.listener, SyncUpdate.SYNC_DONE);
        unbind();
        instance = null;
    }

    /**
     * Bind a feedback popup to the process. Must be started before binding
     * otherwise nothing happens. This will show the popup with the current
     * state.
     */
    public static boolean bind(ProgressPopup feedback, TrackedActivity caller,
            Handler listener) {
        if (instance == null) {
            return false;
        }
        instance.caller = caller;
        instance.feedback = feedback;
        instance.listener = listener;
        // Update from current state
        if (instance.phase == PHASE_DATA) {
            feedback.setMax(SyncUpdate.STEPS);
            feedback.setTitle(instance.ctx.getString(R.string.sync_title));
            feedback.setMessage(instance.ctx.getString(R.string.sync_message));
        } else {
            feedback.setMax(instance.productsToLoad.size());
            feedback.setTitle(instance.ctx.getString(R.string.sync_img_title));
            feedback.setMessage(instance.ctx.getString(R.string.sync_img_message));
        }
        feedback.setProgress(instance.progress);
        feedback.show();
        return true;
    }

    /**
     * Unbind feedback for when the popup is destroyed during the process.
     */
    public static void unbind() {
        if (instance == null) {
            return;
        }
        instance.feedback.dismiss();
        instance.feedback = null;
        instance.listener = null;
        instance.caller = null;
    }

    /**
     * Increment progress by steps and update feedback.
     */
    private void progress(int steps) {
        this.progress += steps;
        if (this.feedback != null) {
            for (int i = 0; i < steps; i++) {
                if (this.phase == PHASE_DATA) {
                    this.feedback.increment(false);
                } else {
                    this.feedback.increment(true);
                }
            }
        }
    }

    /**
     * Increment progress by 1 and update feedback.
     */
    private void progress() {
        this.progress(1);
    }

    /**
     * POOL!!! Let ctx fly and fill the ctx pool with img requests
     */
    private synchronized void pool() {
        int maxSize = this.productsToLoad.size() + this.categoriesToLoad.size()
                + this.paymentModesToLoad.size();
        int prdEnd = this.productsToLoad.size();
        int catEnd = this.productsToLoad.size() + this.categoriesToLoad.size();
        while (openCtxCount < CTX_POOL_SIZE
                && this.nextCtxIdx < maxSize) {
            if (this.nextCtxIdx < prdEnd) {
                this.imgUpdate.loadImage(this.productsToLoad.get(this.nextCtxIdx));
            } else if (this.nextCtxIdx < catEnd) {
                int idx = this.nextCtxIdx - prdEnd;
                this.imgUpdate.loadImage(this.categoriesToLoad.get(idx));
            } else {
                int idx = this.nextCtxIdx - catEnd;
                this.imgUpdate.loadImage(this.paymentModesToLoad.get(idx));
            }
            this.nextCtxIdx++;
            this.openCtxCount++;
        }
        if (this.nextCtxIdx == maxSize && this.openCtxCount == 0) {
            // This is the end
            this.finish();
        }
    }

    /**
     * A pool ctx has finished, release it and refill pool.
     */
    private synchronized void poolDown() {
        this.progress();
        this.openCtxCount--;
        this.pool();
    }

    @Override
    public boolean handleMessage(Message m) {
        switch (m.what) {
            case SyncUpdate.SYNC_ERROR:
                this.failed = true;
                if (m.obj instanceof Exception) {
                    // Response error (unexpected content)
                    Log.i(LOG_TAG, "Server error " + m.obj);
                    Error.showError(R.string.err_server_error, this.caller);
                } else {
                    // String user error
                    String error = (String) m.obj;
                    if ("Not logged".equals(error)) {
                        Log.i(LOG_TAG, "Not logged");
                        SyncUtils.notifyListener(this.listener, SyncUpdate.SYNC_ERROR_NOT_LOGGED);
                    } else {
                        Log.e(LOG_TAG, "Unknown server errror: " + error);
                        Error.showError(R.string.err_server_error, this.caller);
                    }
                }
                this.finish();
                break;
            case SyncUpdate.CONNECTION_FAILED:
                this.failed = true;
                if (m.obj instanceof Exception) {
                    Log.i(LOG_TAG, "Connection error", ((Exception) m.obj));
                    Error.showError(R.string.err_connection_error, this.caller);
                } else {
                    Log.i(LOG_TAG, "Server error " + m.obj);
                    Error.showError(R.string.err_server_error, this.caller);
                }
                this.finish();
                break;

            case SyncUpdate.INCOMPATIBLE_VERSION:
                this.failed = true;
                Error.showError(R.string.err_version_error, instance.caller);
                this.finish();
                break;
            case SyncUpdate.VERSION_DONE:
                this.progress();
                break;

            case SyncUpdate.CASHREG_SYNC_DONE:
                this.progress();
                // Get received cash register
                CashRegister cashReg = (CashRegister) m.obj;
                Data.CashRegister.set(cashReg);
                try {
                    Data.CashRegister.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save cash register", e);
                    Error.showError(R.string.err_save_cash_register,
                            this.caller);
                }
                break;
            case SyncUpdate.CASH_SYNC_DONE:
                this.progress();
                // Get received cash
                Cash cash = (Cash) m.obj;
                Cash current = Data.Cash.currentCash(this.ctx);
                boolean save = false;
                if (current == null) {
                    // No current cash, set it
                    Data.Cash.setCash(cash);
                    save = true;
                } else if (Data.Cash.mergeCurrent(cash)) {
                    save = true;
                } else {
                    // If cash is not opened, erase it
                    if (!current.wasOpened()) {
                        Data.Cash.setCash(cash);
                        save = true;
                    } else {
                        // This is a conflict
                        Error.showError(R.string.err_cash_conflict,
                                instance.caller);
                    }
                }
                if (save) {
                    try {
                        Data.Cash.save(this.ctx);
                    } catch (IOError e) {
                        Log.e(LOG_TAG, "Unable to save cash", e);
                        Error.showError(R.string.err_save_cash, this.caller);
                    }
                }
                break;

            case SyncUpdate.TAXES_SYNC_DONE:
            case SyncUpdate.CATEGORIES_SYNC_DONE:
                this.progress();
                break;
            case SyncUpdate.CATALOG_SYNC_DONE:
                this.progress();
                System.out.println("Catalog done");
                Catalog catalog = (Catalog) m.obj;
                Data.Catalog.setCatalog(catalog);
                try {
                    Data.Catalog.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save catalog", e);
                    Error.showError(R.string.err_save_catalog, this.caller);
                }
                break;
            case SyncUpdate.COMPOSITIONS_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                Data.Composition.compositions = (Map<String, Composition>) m.obj;
                try {
                    Data.Composition.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save compositions", e);
                    Error.showError(R.string.err_save_compositions, this.caller);
                }
                break;

            case SyncUpdate.ROLES_SYNC_DONE:
                this.progress();
                break;
            case SyncUpdate.USERS_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                List<User> users = (List<User>) m.obj;
                Data.User.setUsers(users);
                try {
                    Data.User.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save users", e);
                    Error.showError(R.string.err_save_users, this.caller);
                }
                break;

            case SyncUpdate.CUSTOMERS_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                List<Customer> customers = (List) m.obj;
                Data.Customer.customers = customers;
                try {
                    Data.Customer.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save customers", e);
                    Error.showError(R.string.err_save_customers, this.caller);
                }
                break;

            case SyncUpdate.TARIFF_AREAS_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                List<TariffArea> areas = (List<TariffArea>) m.obj;
                Data.TariffArea.areas = areas;
                try {
                    Data.TariffArea.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save tariff areas", e);
                    Error.showError(R.string.err_save_tariff_areas, this.caller);
                }
                break;
            case SyncUpdate.PAYMENTMODE_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                List<PaymentMode> modes = (List<PaymentMode>) m.obj;
                Data.PaymentMode.setPaymentModes(modes);
                try {
                    Data.PaymentMode.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save payment modes", e);
                    Error.showError(R.string.err_save_payment_modes, this.caller);
                }
                break;

            case SyncUpdate.RESOURCE_SYNC_DONE:
                this.progress();
                try {
                    if (m.obj != null) {
                        String[] resData = (String[]) m.obj;
                        ResourceData.save(this.ctx, resData[0], resData[1]);
                    } else {
                        // TODO: get name from result when API send back name even if null
                        //ResourceData.delete(this.ctx, resData[0]);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to save resource", e);
                    Error.showError(R.string.err_save_resource, this.caller);
                }
                break;
            case SyncUpdate.PLACES_SKIPPED:
                this.progress();
                break;
            case SyncUpdate.PLACES_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                List<Floor> floors = (List<Floor>) m.obj;
                Data.Place.floors = floors;
                try {
                    Data.Place.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save places", e);
                    Error.showError(R.string.err_save_places, this.caller);
                }
                break;

            case SyncUpdate.STOCKS_SKIPPED:
                this.progress(2);
                break;
            case SyncUpdate.LOCATIONS_SYNC_ERROR:
                this.failed = true;
                if (m.obj instanceof Exception) {
                    Log.e(LOG_TAG, "Location sync error", (Exception) m.obj);
                    Error.showError(((Exception) m.obj).getMessage(), this.caller);
                } else {
                    Log.w(LOG_TAG, "Location sync error: unknown location");
                    Error.showError(R.string.err_unknown_location, this.caller);
                }
                break;
            case SyncUpdate.LOCATIONS_SYNC_DONE:
                this.progress();
                break;
            case SyncUpdate.STOCK_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                Map<String, Stock> stocks = (Map<String, Stock>) m.obj;
                Data.Stock.stocks = stocks;
                try {
                    Data.Stock.save(this.ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save stocks", e);
                    Error.showError(R.string.err_save_stocks, this.caller);
                }
                break;
            case SyncUpdate.DISCOUNT_SYNC_DONE:
                this.progress();
                //noinspection unchecked
                ArrayList<Discount> discounts = (ArrayList< Discount>) m.obj;
                Data.Discount.setCollection(discounts);
                try {
                    Data.Discount.save(ctx);
                } catch (IOError e) {
                    Log.e(LOG_TAG, "Unable to save discount", e);
                    Error.showError(R.string.err_save_discount, caller);
                }
                break;

            case SyncUpdate.STOCK_SYNC_ERROR:
                this.failed = true;
                Log.e(LOG_TAG, "Stock sync error", (Exception) m.obj);
                Error.showError(((Exception) m.obj).getMessage(), this.caller);
                break;
            case SyncUpdate.CASHREG_SYNC_NOTFOUND:
                this.failed = true;
                SyncUtils.notifyListener(this.listener, SyncUpdate.CASHREG_SYNC_NOTFOUND);
                this.finish();
                break;

            case SyncUpdate.DISCOUNT_SYNC_ERROR:
            case SyncUpdate.CATEGORIES_SYNC_ERROR:
            case SyncUpdate.TAXES_SYNC_ERROR:
            case SyncUpdate.CATALOG_SYNC_ERROR:
            case SyncUpdate.USERS_SYNC_ERROR:
            case SyncUpdate.CUSTOMERS_SYNC_ERROR:
            case SyncUpdate.CASH_SYNC_ERROR:
            case SyncUpdate.CASHREG_SYNC_ERROR:
            case SyncUpdate.PLACES_SYNC_ERROR:
            case SyncUpdate.COMPOSITIONS_SYNC_ERROR:
            case SyncUpdate.TARIFF_AREA_SYNC_ERROR:
                this.failed = true;
                Error.showError(((Exception) m.obj).getMessage(), this.caller);
                break;

            case SyncUpdate.SYNC_DONE:
                // Data phase finished, load images
                if (!this.failed) {
                    this.runImgPhase();
                } else {
                    this.finish();
                }
                break;

            case ImgUpdate.LOAD_DONE:
                this.poolDown();
                break;
            case ImgUpdate.CONNECTION_FAILED:
                this.failed = true;
                if (instance != null) {
                    if (m.obj instanceof Exception) {
                        Error.showError(((Exception) m.obj).getMessage(),
                                this.caller);
                    } else if (m.obj instanceof Integer) {
                        Error.showError("Code " + m.obj, this.caller);
                    }
                    this.finish();
                }
                break;
        }
        return true;
    }
}
