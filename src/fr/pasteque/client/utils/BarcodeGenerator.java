/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import fr.pasteque.client.models.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BarcodeGenerator {

    //https://developers.google.com/chart/infographics/docs/qr_codes
    private final static int WIDTH = 177;
    private final static int HEIGHT = 177;

    public static BitMatrix generate(String content, int format) {
        try {
            switch (format) {
                case Barcode.QR:
                    return generateQRCode(content, BarcodeGenerator.WIDTH, BarcodeGenerator.HEIGHT);
                case Barcode.EAN13:
                    return generateEAN13(content, BarcodeGenerator.WIDTH, BarcodeGenerator.HEIGHT);
            }
        } catch (WriterException ex) {
            Logger.getLogger(BarcodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static BitMatrix generateQRCode(String content, int width, int height) throws WriterException {
        return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height);

    }

    public static BitMatrix generateEAN13(String content, int width, int height) throws WriterException {
        return new EAN13Writer().encode(content, BarcodeFormat.EAN_13, width, height);

    }

    public static Bitmap toBitmap(BitMatrix matrix) {
        int bitmapWidth = matrix.getWidth();
        int bitmapHeight = matrix.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < bitmapWidth; i++) {
            for (int j = 0; j < bitmapHeight; j++) {
                bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
