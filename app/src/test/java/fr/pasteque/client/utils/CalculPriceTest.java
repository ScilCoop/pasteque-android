
package fr.pasteque.client.utils;

import junit.framework.Assert;
import org.junit.Test;

import static fr.pasteque.client.utils.CalculPrice.getGenericPrice;
import fr.pasteque.client.utils.CalculPrice.Type;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class CalculPriceTest {

    private static final double delta = 0.00001d;
    private final double price = 50d;
    private final double taxe = 0d;
    private final double discount = 0d;

    private void assertEquals(double a, double b) {
        Assert.assertEquals(a, b, delta);
    }

    @Test
    public void removeTaxTest() {
        double taxe = 0.021;
        double control = 48.97160;
        assertEquals(control, CalculPrice.removeTaxe(price, taxe));
    }

    @Test
    public void getPriceWithDiscountWithoutTaxeTest() {
        double discount = 0.2d;
        final double control = 40d;
        assertEquals(control, getGenericPrice(price, discount, taxe, Type.DISCOUNT));
    }

    @Test
    public void getTaxCostTest() {
        double taxe = 0.055;
        double control = 2.75;
        assertEquals(control, getGenericPrice(price, discount, taxe, Type.TAXE_COST));
    }

    @Test
    public void getDiscountCostTest() {
        double discount = 0.234;
        double control = 11.70d;
        assertEquals(control, getGenericPrice(price, discount, taxe, Type.DISCOUNT_COST));
    }

    @Test
    public void getPriceWithTaxeAndDiscount() {
        double discount = 0.1d;
        double taxe = 0.055d;
        final double control = 47.475d;
        assertEquals(control, getGenericPrice(price, discount, taxe, Type.DISCOUNT | Type.TAXE));
    }

    @Test
    public void applyDiscount() {
        final double control = 36d;
        final double a_price = 50;
        final double discount = 0.28;
        assertEquals(control, CalculPrice.applyDiscount(a_price, discount));
    }

    @Test
    public void mergeDiscountTest() {
        double control = 50;
        final double a_price = 50;
        double p_discount = 0.2;
        double t_discount = 0.1;
        double control_discount = 0.28;
        double discount =  CalculPrice.mergeDiscount(p_discount, t_discount);
        assertEquals(control_discount, discount);

    }

    @Test
    public void truncTest() {
        double a = 0.2225d;
        assertEquals(0.223d, CalculPrice.trunc(a, 3));
    }

    // ---Pour permettre l'exécution des test----------------------
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(CalculPriceTest.class);
    }
}