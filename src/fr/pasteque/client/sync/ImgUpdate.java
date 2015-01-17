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

import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.data.ResourceData;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.utils.URLTextGetter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import java.io.IOException;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Updater for product and category images */
public class ImgUpdate {

    private static final String LOG_TAG = "Pasteque/ImgUpdate";
    public static final int LOAD_DONE = 4701;
    public static final int CONNECTION_FAILED = 4702;

    private static final int TYPE_CAT = 1;
    private static final int TYPE_PRD = 2;

    private Context ctx;
    private Handler listener;

    public ImgUpdate(Context ctx, Handler listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    /** Erase all category images. This is a synchronous call. */
    public void resetCategoryImages() throws IOException {
        ImagesData.clearCategories(this.ctx);
    }

    /** Erase all product images. This is a synchronous call. */
    public void resetProductImage() throws IOException {
        ImagesData.clearProducts(this.ctx);
    }

    /** Request and store the image of a category */
    public void loadImage(Category c) {
        String url = SyncUtils.apiUrl(this.ctx);
        Map<String, String> params = SyncUtils.initParams(this.ctx, "ImagesAPI",
                "getCat");
        params.put("id", c.getId());
        URLTextGetter.getBinary(url, params,
                new DataHandler(DataHandler.TYPE_CAT, c.getId()));
    }

    /** Request and store the image of a product */
    public void loadImage(Product p) {
        String url = SyncUtils.apiUrl(this.ctx);
        Map<String, String> params = SyncUtils.initParams(this.ctx, "ImagesAPI",
                "getPrd");
        params.put("id", p.getId());
        URLTextGetter.getBinary(url, params,
                new DataHandler(DataHandler.TYPE_PRD, p.getId()));        
    }

    public void loadImage(PaymentMode pm) {
        String url = SyncUtils.apiUrl(this.ctx);
        Map<String, String> params = SyncUtils.initParams(this.ctx,
                "ImagesAPI", "getPM");
        params.put("id", String.valueOf(pm.getId()));
        URLTextGetter.getBinary(url, params,
                new DataHandler(DataHandler.TYPE_PM,
                        String.valueOf(pm.getId())));
    }

    private class DataHandler extends Handler {
        
        private static final int TYPE_CAT = 1;
        private static final int TYPE_PRD = 2;
        private static final int TYPE_PM = 3;

        private int type;
        private String id;
        
        public DataHandler(int type, String id) {
            this.type = type;
            this.id = id;
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case URLTextGetter.SUCCESS:
                // Parse content
                byte[] img = (byte[]) msg.obj;
                switch (this.type) {
                case TYPE_CAT:
                    try {
                        ImagesData.storeCategoryImage(ctx, this.id, img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO: handle IOException
                    }
                    break;
                case TYPE_PRD:
                    try {
                        ImagesData.storeProductImage(ctx, this.id, img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO: handle IOException
                    }
                    break;
                case TYPE_PM:
                    try {
                        ImagesData.storePaymentModeImage(ctx,
                                Integer.valueOf(this.id), img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO: handle IOException
                    }
                }
                SyncUtils.notifyListener(listener, LOAD_DONE,
                        msg.obj);
                break;
            case URLTextGetter.ERROR:
                ((Exception)msg.obj).printStackTrace();
            case URLTextGetter.STATUS_NOK:
                SyncUtils.notifyListener(listener, CONNECTION_FAILED,
                        msg.obj);
                return;
            }
        }
    }

}