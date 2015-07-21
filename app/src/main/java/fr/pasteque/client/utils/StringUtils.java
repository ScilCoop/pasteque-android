//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.client.utils;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class StringUtils {

    private static final char [] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final NumberFormat cardformat = new DecimalFormat("000000");
    private static final Random cardrandom = new Random();

    /** Creates a new instance of StringUtils */
    private StringUtils() {
    }

    public static String getCardNumber() {
    return cardformat.format(Math.abs(System.currentTimeMillis()) % 1000000L)
         + cardformat.format(Math.abs(cardrandom.nextLong()) % 1000000L);
    }

    public static String encodeXML(String sValue) {

        if (sValue == null) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < sValue.length(); i++) {
                char charToCompare = sValue.charAt(i);
                if (charToCompare == '&') {
                    buffer.append("&amp;");
                } else if (charToCompare == '<') {
                    buffer.append("&lt;");
                } else if (charToCompare == '>') {
                    buffer.append("&gt;");
                } else if (charToCompare == '\"') {
                    buffer.append("&quot;");
                } else {
                    buffer.append(charToCompare);
                }
            }
            return buffer.toString();
        }
    }

    public static String byte2hex(byte[] binput) {

        StringBuffer sb = new StringBuffer(binput.length * 2);
        for (int i = 0; i < binput.length; i++) {
            int high = ((binput[i] & 0xF0) >> 4);
            int low = (binput[i] & 0x0F);
            sb.append(hexchars[high]);
            sb.append(hexchars[low]);
        }
        return sb.toString();
    }

    public static byte [] hex2byte(String sinput) {
        int length = sinput.length();

        if ((length & 0x01) != 0) {
            throw new IllegalArgumentException("odd number of characters.");
        }

        byte[] out = new byte[length >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < length; i++) {
            int f = Character.digit(sinput.charAt(j++), 16) << 4;
            f = f | Character.digit(sinput.charAt(j++), 16);
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    public static String readResource(String resource) throws IOException {

        InputStream in = StringUtils.class.getResourceAsStream(resource);
        if (in == null) {
            throw new FileNotFoundException(resource);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        byte[] data = out.toByteArray();

        return new String(data, "UTF-8");
    }

    public static boolean isNumber(String sCardNumber){

        if ( (sCardNumber==null) || (sCardNumber.equals("")) ){
            return false;
        }

        for (int i = 0; i < sCardNumber.length(); i++) {
            char c = sCardNumber.charAt(i);
            if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9') {
                return false;
            }
        }

        return true;
    }

    /**
     * The function transforms milliseconds date into dd/mm/yyyy using locale order fields.
     * Date string length is constant.
     * @param mils are the milliseconds since 01 jan, 1970
     */
    public static String formatDateNumeric(Context ctx, long mils) {
        final int flags = DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE;
        String dateString = DateUtils.formatDateTime(ctx, mils, flags);
        String[] dateSplit = dateString.split("/");
        int length = dateSplit.length;
        for (int i = 0; i < length; ++i) {
            if (dateSplit[i].length() < 2) dateSplit[i] = "0" + dateSplit[i];
        }
        return TextUtils.join("/", dateSplit);
    }

    public static String formatToCurrency(double money) {
        //TODO: make currency locale customizable.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return nf.format(money);
    }
}
