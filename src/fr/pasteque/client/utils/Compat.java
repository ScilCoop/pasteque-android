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
package fr.pasteque.client.utils;

import android.content.Context;
import com.payleven.payment.api.PaylevenApi;

/** Compatibility checker */
public class Compat {

    public static int getAPILevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static boolean isEpsonPrinterCompatible() {
        return Compat.getAPILevel() >= 10; // 2.3.3
    }

    public static boolean isLKPXXPrinterCompatible() {
        return Compat.getAPILevel() >= 7; // 2.1
    }

    public static boolean isWoosimPrinterCompatible() {
        return Compat.getAPILevel() >= 5; // 2.0
    }

    public static boolean hasPaylevenApp(Context ctx) {
        return PaylevenApi.isPaylevenAvailable(ctx);
    }
}
