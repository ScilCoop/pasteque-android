/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.models.Discount;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Save and Load discounts in FILENAME Data can be accessed anywhere, anytime.
 *
 * @author nsvir
 */
public final class DiscountData {

    private static final String FILENAME = "discount.data";

    private static ArrayList<Discount> discounts;

    public static void addDiscount(Discount disc) {
        discounts.add(disc);
    }

    public static void setCollection(ArrayList<Discount> discounts) {
        DiscountData.discounts = discounts;
    }

    public static ArrayList<Discount> getDiscounts() {
        return discounts;
    }

    public static boolean load(Context ctx) throws Exception {
        FileInputStream fis = ctx.openFileInput(FILENAME);
        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            discounts = (ArrayList<Discount>) ois.readObject();
        }
        return true;
    }

    public static boolean save(Context ctx) throws Exception {
        FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(discounts);
        }
        return true;
    }

    public static Discount getADiscount() {        
        if (DiscountData.discounts.isEmpty())
            throw new RuntimeException("No discounts in DiscountData");
        return discounts.get(0);
    }
}
