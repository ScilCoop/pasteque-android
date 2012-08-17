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
package fr.postech.client.utils;

import android.content.Context;
import android.os.Message;
import android.os.Handler;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


import fr.postech.client.models.Product;

public class URLTextGetter {

    public static final int SUCCESS = 0;
    public static final int STATUS_NOK = 1;
    public static final int ERROR = 2;

    public static void getText(final String url, final Handler h) {
        getText(url, null, h);
    }
    
    public static void getText(final String url, final Map<String, String> post,
                               final Handler h) {
        new Thread() {
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response = null;
                    if (post == null) {
                        HttpGet req = new HttpGet(url);
                        response = client.execute(req);
                    } else {
                        HttpPost req = new HttpPost(url);
                        List<NameValuePair> args = new ArrayList<NameValuePair>();
                        for(String key : post.keySet()) {
                            String value = post.get(key);
                            args.add(new BasicNameValuePair(key, value));
                        }
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(args,HTTP.UTF_8);
                        req.setEntity(entity);
                        response = client.execute(req);
                    }
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
                        if (h != null) {
                            Message m = h.obtainMessage();
                            m.what = SUCCESS;
                            m.obj = content;
                            m.sendToTarget();
                        }
                    } else {
                        if (h != null) {
                            Message m = h.obtainMessage();
                            m.what = STATUS_NOK;
                            m.obj = new Integer(status);
                            m.sendToTarget();
                        }
                    }
                } catch( IOException e ) {
                    e.printStackTrace();
                    if (h != null) {
                        Message m = h.obtainMessage();
                        m.what = ERROR;
                        m.obj = e;
                        m.sendToTarget();
                    }
                }
            }
        }.start();
    }

}