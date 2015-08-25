package fr.pasteque.client.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by nsvir on 24/08/15.
 * n.svirchevsky@gmail.com
 */
public class CalculPrice {

    public class Type {
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

    public static final double getDiscountCost(double price, double discount) {
        return trunc(price * discount);
    }

    public static final double applyDiscount(double price, double discount) {
        return trunc(price * (1 - discount));
    }

    public static final double mergeDiscount(double productDiscount, double ticketDiscount) {
        return trunc(productDiscount + ticketDiscount - (productDiscount * ticketDiscount));
    }

    public static double applyTax(double price, double taxRate) {
        return trunc(price + getTaxCost(price, taxRate));
    }

    public static double getTaxCost(double price, double taxRate) {
        return trunc(price * taxRate);
    }

    public static double trunc(double number) {
        return trunc(number, DEFAULT_DECIMAL_NUMBER);
    }

    public static double trunc(double number, int decimalNumber) {
        return new BigDecimal(number).setScale(decimalNumber, RoundingMode.HALF_UP).doubleValue();
    }

}
