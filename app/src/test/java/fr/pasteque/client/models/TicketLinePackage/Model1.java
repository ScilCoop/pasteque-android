package fr.pasteque.client.models.TicketLinePackage;

import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.models.TicketLine;

/**
 * Created by nsvir on 27/08/15.
 * n.svirchevsky@gmail.com
 */
public class Model1 {

    public final static String id = "";
    public final static String label = "";
    public final static String barcode = "";
    public final static double price = 10d;
    public final static String taxid = "";
    public final static double taxrate = 0.055d;
    public final static boolean scaled = false;
    public final static boolean hasImage = false;
    public final static double discountrate = 0.02d;
    public final static boolean discountrateenabled = true;

    public final static TariffArea area = null;
    public final static int quantity = 3;

    public final static double defaultTicketDiscount = 0.02d;

    public double controlTotalTaxCost = 0.2d;
    public double controlProductTaxCost = 0.2d;


    public final static double[][] control_product =
            {
                    //None
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    },

                    //DISC_P
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    },

                    //DISC
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    }
            };

    public final static double[][] control_total =
            {
                    //None
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    },

                    //DISC_P
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    },

                    //DISC
                    {
                            //includeTax
                            0d,
                            //ExcludeTax
                            0d
                    }
            };

    public final static double[][][] control = {
            control_product,
            control_total};

    public static Product newProduct() {
        return new Product(id, label, barcode, price, taxid, taxrate, scaled, hasImage,
                discountrate, discountrateenabled);
    }

    public TicketLine newTicketLine(Product product) {
        return new TicketLine(product, quantity, area);
    }
}
