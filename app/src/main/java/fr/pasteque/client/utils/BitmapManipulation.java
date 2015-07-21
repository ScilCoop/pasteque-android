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

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by nsvir on 03/07/15.
 */
public class BitmapManipulation {
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
