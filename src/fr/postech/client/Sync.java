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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
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

import fr.postech.client.models.Product;

public class Sync {

    public static final int SYNC_DONE = 1;
    public static final int CONNECTION_FAILED = 2;
    public static final int CATALOG_SYNC_DONE = 3;

    /** Launch synchronization and display progress dialog */
    public static void startSync(Context ctx, final Handler h) {
        final ProgressDialog p = new ProgressDialog(ctx);
        p.setMax(3);
        p.show();
        synchronize(ctx, new Handler() {
                @Override
                public void handleMessage(Message m) {
                    p.dismiss();
                    Message m2 = m.obtain(m);
                    m2.setTarget(h);
                    m2.sendToTarget();
                }
            });
    }

    public static void synchronize(final Context ctx, final Handler h) {
        new Thread() {
            public void run() {
                try {
                    String url = "http://" + PreferenceManager.getDefaultSharedPreferences(ctx).getString("host", "") + "/api/ProductsAPI?action=getAllFull";
                    System.out.println(url);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet req = new HttpGet(url);
                    HttpResponse response = client.execute(req);
                    int status = response.getStatusLine().getStatusCode();
                    if(status == HttpStatus.SC_OK) {
                        // Get http response
                        String content = "";
                        try {
                            final int size = 1024;
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
                            byte[] buffer = new byte[size];
                            BufferedInputStream bis = new BufferedInputStream( response.getEntity().getContent() );
                            int read = bis.read(buffer, 0, size);
                            while (read != -1) {
                                bos.write(buffer, 0, read);
                                read = bis.read(buffer, 0, size);
                            }
                            content = new String(bos.toByteArray());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        // Parse and return it
                        System.out.println(content);
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
                        if (h != null) {
                            Message m = h.obtainMessage();
                            m.what = CATALOG_SYNC_DONE;
                            m.obj = products;
                            m.sendToTarget();
                        }
                    } else {
                        if (h != null) {
                            Message m = h.obtainMessage();
                            m.what = CONNECTION_FAILED;
                            m.obj = status;
                            m.sendToTarget();
                        }                        
                    }
                } catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}