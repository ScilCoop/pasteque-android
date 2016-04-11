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
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.utils.file.File;
import fr.pasteque.client.utils.file.InternalFile;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Product, categories and other image data loader
 */
public class ImagesData {

    private static final String PRODUCT_PREFIX = "img_prd_";
    private static final String CATEGORY_PREFIX = "img_cat_";
    private static final String PAYMENTMODE_PREFIX = "img_pm_";
    private static final String IMAGE_DIRECTORY = "images";

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

    public static void clearPaymentModes(Context ctx)
            throws IOException {
        for (String file : ctx.fileList()) {
            if (file.startsWith(PAYMENTMODE_PREFIX)) {
                ctx.deleteFile(file);
            }
        }
    }

    private static Bitmap getBitmap(String s) {
        try {
            FileInputStream fis = getFileInputStream(s);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    private static void write(FileOutputStream fileOutputStream, byte[] data) throws IOException {
        fileOutputStream.write(data, 0, data.length);
        fileOutputStream.close();
    }

    private static FileOutputStream getFileOutputStream(String s) throws FileNotFoundException {
        return new FileOutputStream(new InternalFile(IMAGE_DIRECTORY, s));
    }

    private static FileInputStream getFileInputStream(String s) throws FileNotFoundException {
        return new FileInputStream(new InternalFile(IMAGE_DIRECTORY, s));
    }

    public static Bitmap getCategoryImage(String categoryId) {
        return getBitmap(CATEGORY_PREFIX + categoryId);
    }

    public static Bitmap getProductImage(String productId) {
        return getBitmap(PRODUCT_PREFIX + productId);
    }

    public static Bitmap getPaymentModeImage(int paymentModeId)
            throws IOException {
        return getBitmap(PAYMENTMODE_PREFIX
                + paymentModeId);
    }

    public static void storeCategoryImage(String categoryId, byte[] data)
            throws IOException {
        write(getFileOutputStream(CATEGORY_PREFIX + categoryId), data);
    }

    public static void storeProductImage(String productId, byte[] data)
            throws IOException {
        write(getFileOutputStream(PRODUCT_PREFIX + productId), data);
    }

    public static void storePaymentModeImage(int paymentModeId,
                                             byte[] data)
            throws IOException {
        write(getFileOutputStream(PAYMENTMODE_PREFIX
                + paymentModeId), data);
    }

}
