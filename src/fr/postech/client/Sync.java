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

import fr.postech.client.models.User;
import fr.postech.client.models.Product;
import fr.postech.client.utils.HostParser;
import fr.postech.client.utils.URLTextGetter;

public class Sync {

    public static final int SYNC_DONE = 1;
    public static final int CONNECTION_FAILED = 2;
    public static final int CATALOG_SYNC_DONE = 3;
    public static final int USERS_SYNC_DONE = 4;

    private Context ctx;
    private Handler listener;
    private boolean productsDone;
    private boolean usersDone;

    private ProgressDialog progress;

    public Sync(Context ctx, Handler listener) {
        this.listener = listener;
        this.ctx = ctx;
    }

    /** Launch synchronization and display progress dialog */
    public void startSync() {
        this.progress = new ProgressDialog(ctx);
        this.progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progress.setMax(2);
        this.progress.setTitle(ctx.getString(R.string.sync_title));
        this.progress.setMessage(ctx.getString(R.string.sync_message));
        this.progress.show();
        synchronize();
    }

    public void synchronize() {
        String baseUrl = HostParser.getHostFromPrefs(this.ctx);
        String productsUrl = baseUrl + "api/ProductsAPI?action=getAllFull";
        String usersUrl = baseUrl + "api/UsersAPI?action=getAll";
        URLTextGetter.getText(productsUrl, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Sync.this.productsDone = true;
                    if (progress != null) {
                        progress.incrementProgressBy(1);
                    }
                    switch (msg.what) {
                    case URLTextGetter.SUCCESS:
                        // Parse and return it
                        String content = (String) msg.obj;
                        List<Product> products = new ArrayList<Product>();
                        try {
                            JSONArray array = new JSONArray(content);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                Product p = Product.fromJSON(o);
                                products.add(p);
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                            // TODO: shouldn't break parsing
                        }
                        if (listener != null) {
                            Message m = listener.obtainMessage();
                            m.what = CATALOG_SYNC_DONE;
                            m.obj = products;
                            m.sendToTarget();
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
            });
        URLTextGetter.getText(usersUrl, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Sync.this.usersDone = true;
                    if (progress != null) {
                        progress.incrementProgressBy(1);
                    }
                    switch (msg.what) {
                    case URLTextGetter.SUCCESS:
                        // Parse and return it
                        String content = (String) msg.obj;
                        List<User> users = new ArrayList<User>();
                        try {
                            JSONArray array = new JSONArray(content);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                User u = User.fromJSON(o);
                                users.add(u);
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                            // TODO: shouldn't break parsing
                        }
                        if (listener != null) {
                            Message m = listener.obtainMessage();
                            m.what = USERS_SYNC_DONE;
                            m.obj = users;
                            m.sendToTarget();
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
            });
    }
    
    private void checkFinished() {
        if (this.productsDone && this.usersDone) {
            if (this.progress != null) {
                this.progress.dismiss();
                this.progress = null;
            }
            Message m = this.listener.obtainMessage();
            m.what = SYNC_DONE;
            m.sendToTarget();
        }
    }
}