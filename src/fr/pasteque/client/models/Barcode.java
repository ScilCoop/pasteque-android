/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.models;

public class Barcode {

    public final static int NONE = 0;
    public final static int EAN13 = 1;
    public final static int QR = 2;

    public static String toString(int value) {
        switch (value) {
            case Barcode.QR:
                return "QR";
            case Barcode.EAN13:
                return "EAN13";
        }
        return "";
    }
}
