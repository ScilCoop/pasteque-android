/*
 * Pasteque Android client
 * Copyright (C) Pasteque contributors, see the COPYRIGHT file
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.pasteque.client.utils;

import android.graphics.*;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.IOException;

/**
 * Created by nsvir on 03/07/15.
 * n.svirchevsky@gmail.com
 */
public class BitmapManipulation {

    // Based on StarIO Android SDK
    public static Bitmap createBitmapFromText(String printText, int textSize, int printWidth, Typeface typeface) {
        Paint paint = new Paint();
        Bitmap bitmap;
        Canvas canvas;

        paint.setTextSize(textSize);
        paint.setTypeface(typeface);

        paint.getTextBounds(printText, 0, printText.length(), new Rect());

        TextPaint textPaint = new TextPaint(paint);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textPaint, printWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

        // Create bitmap
        bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(), Bitmap.Config.ARGB_8888);

        // Create canvas
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.translate(0, 0);
        staticLayout.draw(canvas);

        return bitmap;
    }

    public static Bitmap createBitmapFromResources(String bitmapString) throws IOException {
        byte[] imageAsBytes = Base64.decode(bitmapString.getBytes());
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static Bitmap centeredBitmap(Bitmap img, int resultWidth) {
        if (img == null || resultWidth <= img.getWidth())
            return img;
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int offset = (resultWidth - imgWidth) / 2;
        Bitmap result = Bitmap.createBitmap(resultWidth, imgHeight, img.getConfig());
        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < resultWidth; x++) {
                if (x >= offset && x < (imgWidth + offset)) {
                    result.setPixel(x, y, img.getPixel(x - offset, y));
                } else {
                    result.setPixel(x, y, Color.WHITE);
                }
            }
        }
        return result;
    }
}
