/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.models;

import android.graphics.Bitmap;
import fr.pasteque.client.utils.BarcodeGenerator;

import java.io.Serializable;

public class Barcode implements Serializable {

    public class Prefix {
        public final static String DISCOUNT = "DISC_";
    }

    public final static int NONE = 0;
    public final static int EAN13 = 1;
    public final static int QR = 2;

    private String code;
    private int type;

    public Barcode(String code, int type) {
        this.code = code;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bitmap toBitmap() {
        return BarcodeGenerator.generate(this.code, this.type);
    }
}