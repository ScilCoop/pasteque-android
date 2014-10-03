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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/** Product, categories and other image data loader */
public class ImagesData {

    private static final String PRODUCT_PREFIX = "img_prd_";
    private static final String CATEGORY_PREFIX = "img_cat_";

    public static void clearProducts(Context ctx)
        throws IOException {
        for (String file : ctx.fileList()) {
            if (file.startsWith(PRODUCT_PREFIX)) {
                ctx.deleteFile(file);
            }
        }
    }

    public static void clearCategories(Context ctx)
        throws IOException {
        for (String file : ctx.fileList()) {
            if (file.startsWith(CATEGORY_PREFIX)) {
                ctx.deleteFile(file);
            }
        }
    }

    public static Bitmap getCategoryImage(Context ctx, String categoryId)
        throws IOException {
        try {
            FileInputStream fis = ctx.openFileInput(CATEGORY_PREFIX + categoryId);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void storeCategoryImage(Context ctx, String categoryId, byte[] data)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(CATEGORY_PREFIX + categoryId,
                ctx.MODE_PRIVATE);
        fos.write(data, 0, data.length);
        fos.close();
    }

    public static Bitmap getProductImage(Context ctx, String productId)
        throws IOException {
        try {
            FileInputStream fis = ctx.openFileInput(PRODUCT_PREFIX + productId);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void storeProductImage(Context ctx, String productId, byte[] data)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(PRODUCT_PREFIX + productId,
                ctx.MODE_PRIVATE);
        fos.write(data, 0, data.length);
        fos.close();
    }
}
