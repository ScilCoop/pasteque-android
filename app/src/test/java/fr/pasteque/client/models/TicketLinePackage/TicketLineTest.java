package fr.pasteque.client.models.TicketLinePackage;

import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.TicketLine;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by nsvir on 27/08/15.
 * n.svirchevsky@gmail.com
 */
@RunWith(PowerMockRunner.class)
public class TicketLineTest {

    public final static int PRODUCT = 0;
    public final static int TOTAL = 1;
    public final static int NO_DISC = 0;
    public final static int DISC_P = 1;
    public final static int DISC = 2;
    public final static int INC = 0;
    public final static int EXT = 1;

    private final double delta = 0.00001d;
    private final Model1 model = new Model1();

    private final double [][][] control = model.control;

    private double ticketDiscount;
    private Product product;
    private TicketLine ticketLine;

    @Before
    public void setUp() throws Exception {
        product = model.newProduct();
        ticketLine = model.newTicketLine(product);
        ticketDiscount = model.defaultTicketDiscount;
    }

    @Test
    public void getProductDiscExcTaxTest() {
        assertEquals(control[PRODUCT][DISC][EXT],
                ticketLine.getProductDiscExcTax(ticketDiscount));
    }

    @Test
    public void getProductDiscPExcTaxTest() {
        assertEquals(control[PRODUCT][DISC_P][EXT],
                ticketLine.getProductDiscPExcTax());
    }

    @Test
    public void getProductExcTaxTest() {
        assertEquals(control[PRODUCT][NO_DISC][EXT],
                ticketLine.getProductExcTax());
    }

    @Test
    public void getProductIncTaxTest() {
        assertEquals(control[PRODUCT][NO_DISC][INC],
                ticketLine.getProductIncTax());
    }

    @Test
    public void getProductTaxCostTest() {
        assertEquals(model.controlProductTaxCost,
                ticketLine.getProductTaxCost(ticketDiscount));
    }

    @Test
    public void getTotalDiscExcTaxTest() {
        assertEquals(control[TOTAL][DISC][EXT],
                ticketLine.getTotalDiscExcTax(ticketDiscount));
    }

    @Test
    public void getTotalDiscIncTaxTest() {
        assertEquals(control[TOTAL][DISC][INC],
                ticketLine.getTotalDiscIncTax(ticketDiscount));
    }

    @Test
    public void getTotalDiscPExcTaxTest() {
        assertEquals(control[TOTAL][DISC_P][EXT],
                ticketLine.getTotalDiscPExcTax());
    }

    @Test
    public void getTotalDiscPIncTaxTest() {
        assertEquals(control[TOTAL][DISC_P][INC],
                ticketLine.getTotalDiscPIncTax());
    }

    @Test
    public void getTotalExcTaxTest() {
        assertEquals(control[TOTAL][NO_DISC][EXT],
                ticketLine.getTotalExcTax());
    }

    @Test
    public void getTotalIncTaxTest() {
        assertEquals(control[TOTAL][NO_DISC][INC],
                ticketLine.getTotalIncTax());
    }

    @Test
    public void getTotalTaxCostTest() {
        assertEquals(model.controlTotalTaxCost,
                ticketLine.getTotalTaxCost(ticketDiscount));
    }

    private void assertEquals(double a, double b) {
        Assert.assertEquals(a, b, delta);
    }

    // ---Pour permettre l'exécution des test----------------------
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TicketLineTest.class);
    }
}
