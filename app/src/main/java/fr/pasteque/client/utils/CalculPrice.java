package fr.pasteque.client.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by nsvir on 24/08/15.
 * n.svirchevsky@gmail.com
 */
public class CalculPrice {

    public class Type {
        public static final int NONE = 0;
        public static final int DISCOUNT = 1;
        public static final int DISCOUNT_COST = 2;
        public static final int TAXE = 4;
        public static final int TAXE_COST = 8;
    }

    public static final int DEFAULT_DECIMAL_NUMBER = 5;

    private static boolean hasOption(int binaryMask, int model) {
        return (binaryMask & model) == model;
    }

    public static final double getGenericPrice(double price, double discount, double taxe, int binaryMask) {
        if (hasOption(binaryMask, Type.DISCOUNT_COST)) {
            return getDiscountCost(price, discount);
        }
        if (hasOption(binaryMask, Type.TAXE_COST)) {
            return getTaxCost(price, taxe);
        }
        if (hasOption(binaryMask, Type.DISCOUNT)) {
            price = applyDiscount(price, discount);
        }
        if (hasOption(binaryMask, Type.TAXE)) {
            price = applyTax(price, taxe);
        }
        return price;
    }

    public static final double removeTaxe(double price, double tax) {
        return round(price / (1 + tax));
    }

    public static final double getDiscountCost(double price, double discount) {
        return round(price * discount);
    }

    public static final double applyDiscount(double price, double discount) {
        return round(price * (1 - discount));
    }

    public static final double mergeDiscount(double productDiscount, double ticketDiscount) {
        return round(productDiscount + ticketDiscount - (productDiscount * ticketDiscount));
    }

    public static double applyTax(double price, double taxRate) {
        return round(price + getTaxCost(price, taxRate));
    }

    public static double getTaxCost(double price, double taxRate) {
        return round(price * taxRate);
    }

    public static double round(double number) {
        return round(number, DEFAULT_DECIMAL_NUMBER);
    }

    public static double round(double number, int decimalNumber) {
        return new BigDecimal(number).setScale(decimalNumber, RoundingMode.HALF_UP).doubleValue();
    }

}
