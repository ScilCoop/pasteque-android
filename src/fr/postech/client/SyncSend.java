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
package fr.postech.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import fr.postech.client.models.Cash;
import fr.postech.client.models.Catalog;
import fr.postech.client.models.Category;
import fr.postech.client.models.User;
import fr.postech.client.models.Product;
import fr.postech.client.models.Receipt;
import fr.postech.client.utils.HostParser;
import fr.postech.client.utils.URLTextGetter;

public class SyncSend {

    // Note: SyncUpdate uses positive values, SyncSend negative ones
    public static final int SYNC_DONE = -1;
    public static final int CONNECTION_FAILED = -2;
    public static final int RECEIPTS_SYNC_DONE = -3;
    public static final int RECEIPTS_SYNC_FAILED = -4;

    private Context ctx;
    private Handler listener;

    private ProgressDialog progress;
    /** The tickets to send */
    private List<Receipt> receipts;
    private Cash cash;
    private boolean receiptsDone;

    public SyncSend(Context ctx, Handler listener,
                    List<Receipt> receipts, Cash cash) {
        this.listener = listener;
        this.ctx = ctx;
        this.receipts = receipts;
        this.cash = cash;
    }

    /** Launch synchronization and display progress dialog */
    public void startSyncSend() {
        this.progress = new ProgressDialog(ctx);
        this.progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progress.setMax(1);
        this.progress.setTitle(ctx.getString(R.string.sync_title));
        this.progress.setMessage(ctx.getString(R.string.sync_message));
        this.progress.show();
        synchronize();
    }

    public void synchronize() {
        String baseUrl = HostParser.getHostFromPrefs(this.ctx);
        String ticketsUrl = baseUrl + "api/TicketsAPI?action=save";
        JSONArray rcptsJSON = new JSONArray();
        for (Receipt r : this.receipts) {
            try {
                JSONObject o = r.toJSON();
                rcptsJSON.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Map<String, String> postBody = new HashMap<String, String>();
        postBody.put("tickets", rcptsJSON.toString());
        try {
            postBody.put("cash", this.cash.toJSON().toString());
        } catch (JSONException e) {
            // This breaks it all, even if it should never happen
            e.printStackTrace();
            return; // TODO: handle the fatal error
        }
        URLTextGetter.getText(ticketsUrl, postBody,
                              new DataHandler(DataHandler.TYPE_RECEIPTS));
    }

    private void parseReceiptsResult(String result) {
        int what = 0;
        if (result.equals("true")) {
            what = RECEIPTS_SYNC_DONE;
        } else {
            what = RECEIPTS_SYNC_FAILED;
        }
        if (this.listener != null) {
            Message m = listener.obtainMessage();
            m.what = what;
            m.obj = result;
            m.sendToTarget();
        }
    }

    private void checkFinished() {
        if (this.receiptsDone) {
            if (this.progress != null) {
                this.progress.dismiss();
                this.progress = null;
            }
            Message m = this.listener.obtainMessage();
            m.what = SYNC_DONE;
            m.sendToTarget();
        }
    }

    
    private class DataHandler extends Handler {
        
        private static final int TYPE_RECEIPTS = 1;

        private int type;
        
        public DataHandler(int type) {
            this.type = type;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (this.type) {
            case TYPE_RECEIPTS:
                SyncSend.this.receiptsDone = true;
                break;
            }
            if (progress != null) {
                progress.incrementProgressBy(1);
            }
            switch (msg.what) {
            case URLTextGetter.SUCCESS:
                // Parse content
                String content = (String) msg.obj;
                switch (type) {
                case TYPE_RECEIPTS:
                    parseReceiptsResult(content);
                    break;
                }
                break;
            case URLTextGetter.ERROR:
                ((Exception)msg.obj).printStackTrace();
            case URLTextGetter.STATUS_NOK:
                if (listener != null) {
                    Message m = listener.obtainMessage();
                    m.what = CONNECTION_FAILED;
                    m.obj = msg.obj;
                    m.sendToTarget();
                }
                break;
            }
            checkFinished();
        }
    }

}