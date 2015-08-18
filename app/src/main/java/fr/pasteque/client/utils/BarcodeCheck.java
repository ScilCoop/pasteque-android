package fr.pasteque.client.utils;

/**
 * Created by nsvir on 18/08/15.
 * n.svirchevsky@gmail.com
 */
public class BarcodeCheck {

    private static final int[] ean13_weighting = new int[]{1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3};

    private static int getCheckDigit(byte[] barcode) {
        int result = 0;
        for (int i = 0; i < 12; i++) {
            result += Character.getNumericValue(barcode[i]) * ean13_weighting[i];
        }
        return (10 - (result % 10)) % 10;
    }

    public static boolean ean13(String barcode) {
        return barcode.length() == 13
                && BarcodeCheck.getCheckDigit(barcode.getBytes()) == Character.getNumericValue(barcode.getBytes()[12]);
    }
}
