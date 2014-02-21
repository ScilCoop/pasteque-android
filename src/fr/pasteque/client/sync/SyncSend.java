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

import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.pasteque.client.Configure;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.User;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.utils.HostParser;
import fr.pasteque.client.utils.URLTextGetter;

public class SyncSend {

    private static final String LOG_TAG = "Pasteque/SyncSend";

    public static final int STEPS = 2;
    // Note: SyncUpdate uses positive values, SyncSend negative ones
    public static final int SYNC_DONE = -1;
    public static final int CONNECTION_FAILED = -2;
    public static final int RECEIPTS_SYNC_DONE = -3;
    public static final int RECEIPTS_SYNC_FAILED = -4;
    public static final int CASH_SYNC_DONE = -5;
    public static final int CASH_SYNC_FAILED = -6;
    public static final int EPIC_FAIL = -7;
    public static final int SYNC_ERROR = -8;

    private Context ctx;
    private Handler listener;

    /** The tickets to send */
    private List<Receipt> receipts;
    private Cash cash;
    private boolean receiptsDone;
    private boolean cashDone;
    private boolean killed;

    private String apiUrl() {
        return HostParser.getHostFromPrefs(this.ctx) + "api.php";
    }

    private Map<String, String> initParams(String service, String action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("login", Configure.getUser(this.ctx));
        params.put("password", Configure.getPassword(this.ctx));
        params.put("p", service);
        if (action != null) {
            params.put("action", action);
        }
        return params;
    }

    public SyncSend(Context ctx, Handler listener,
                    List<Receipt> receipts, Cash cash) {
        this.listener = listener;
        this.ctx = ctx;
        this.receipts = receipts;
        this.cash = cash;
    }

    public void synchronize() {
        runCashSync();
     }

    private void fail(Exception e) {
        if (this.listener != null) {
            Message m = listener.obtainMessage();
            m.what = CASH_SYNC_FAILED;
            m.obj = e;
            m.sendToTarget();
        }
    }

    private void runReceiptsSync() {
        if (this.receipts.size() == 0) {
            // No receipts, skip and notify
            SyncSend.this.receiptsDone = true;
            int what = 0;
            what = RECEIPTS_SYNC_DONE;
            if (this.listener != null) {
                Message m = listener.obtainMessage();
                m.what = what;
                m.obj = true;
                m.sendToTarget();
            }
            this.checkFinished();
            return;
        }
        JSONArray rcptsJSON = new JSONArray();
        for (Receipt r : this.receipts) {
            try {
                JSONObject o = r.toJSON();
                rcptsJSON.put(o);
            } catch (JSONException e) {
                Log.e(LOG_TAG, r.toString(), e);
                this.fail(e);
                return;
            }
        }
        Map<String, String> postBody = this.initParams("TicketsAPI", "save");
        postBody.put("tickets", rcptsJSON.toString());
        postBody.put("cash_id", this.cash.getId());
        String location = Configure.getStockLocation(this.ctx);
        if (!location.equals("")) {
            postBody.put("location", location);
        }
        URLTextGetter.getText(this.apiUrl(), null,
                postBody, new DataHandler(DataHandler.TYPE_RECEIPTS));
    }

    private void runCashSync() {
        Map<String, String> postBody = this.initParams("CashesAPI", "update");
        try {
            postBody.put("cash", this.cash.toJSON().toString());
        } catch (JSONException e) {
            Log.e(LOG_TAG, this.cash.toString(), e);
            this.fail(e);
            return;
        }
        URLTextGetter.getText(this.apiUrl(), null, postBody,
                new DataHandler(DataHandler.TYPE_CASH));
    }

    private void parseReceiptsResult(JSONObject resp) {
        int what = 0;
        // If service succeed it always return true
        what = RECEIPTS_SYNC_DONE;
        if (this.listener != null) {
            Message m = listener.obtainMessage();
            m.what = what;
            m.obj = this.cash;
            m.sendToTarget();
        }
    }

    private void parseCashResult(JSONObject resp) {
        try {
            JSONObject o = resp.getJSONObject("content");
            Cash cash = Cash.fromJSON(o);
            // Update our cash for tickets (maybe id is set)
            this.cash = cash;
            if (this.listener != null) {
                Message m = listener.obtainMessage();
                m.what = CASH_SYNC_DONE;
                m.obj = cash;
                m.sendToTarget();
            }
            // Continue with receipts
            this.runReceiptsSync();
        } catch(JSONException e) {
            Log.e(LOG_TAG, "Error while parsing cash result", e);
            if (this.listener != null) {
                Message m = listener.obtainMessage();
                m.what = CASH_SYNC_FAILED;
                m.obj = resp;
                m.sendToTarget();
            }
            return;
        }
    }

    private void finish() {
        Message m = this.listener.obtainMessage();
        m.what = SYNC_DONE;
        m.sendToTarget();
    }

    private void checkFinished() {
        if (this.receiptsDone && this.cashDone) {
            this.finish();
        }
    }

    
    private class DataHandler extends Handler {
        
        private static final int TYPE_RECEIPTS = 1;
        private static final int TYPE_CASH = 2;

        private int type;
        
        public DataHandler(int type) {
            this.type = type;
        }

        private String getError(String response) {
            try {
                JSONObject o = new JSONObject(response);
                if (o.has("error")) {
                    return o.getString("error");
                }
            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (this.type) {
            case TYPE_RECEIPTS:
                SyncSend.this.receiptsDone = true;
                break;
            case TYPE_CASH:
                SyncSend.this.cashDone = true;
                break;
            }
            switch (msg.what) {
            case URLTextGetter.SUCCESS:
                // Parse content
                String content = (String) msg.obj;
                try {
                    JSONObject result = new JSONObject(content);
                    String status = result.getString("status");
                    if (!status.equals("ok")) {
                        JSONObject err = result.getJSONObject("content");
                        String error = err.getString("code");
                        if (listener != null) {
                            Message m = listener.obtainMessage();
                            m.what = SYNC_ERROR;
                            m.obj = error;
                            m.sendToTarget();
                        }
                        finish();
                    } else {
                        switch (type) {
                        case TYPE_RECEIPTS:
                            parseReceiptsResult(result);
                            break;
                        case TYPE_CASH:
                            parseCashResult(result);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    if (listener != null) {
                        Message m = listener.obtainMessage();
                        m.what = SYNC_ERROR;
                        m.obj = e;
                        m.sendToTarget();
                    }
                    finish();
                }
                break;
            case URLTextGetter.ERROR:
                Log.e(LOG_TAG, "URLTextGetter error", (Exception)msg.obj);
            case URLTextGetter.STATUS_NOK:
                if (listener != null) {
                    Message m = listener.obtainMessage();
                    m.what = CONNECTION_FAILED;
                    m.obj = msg.obj;
                    m.sendToTarget();
                }
                finish();
                return;
            }
            checkFinished();
        }
    }

}
