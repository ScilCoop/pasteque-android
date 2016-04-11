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
    public final static double discountrate = 0.2d;
    public final static boolean discountrateenabled = true;

    public final static TariffArea area = null;
    public final static double quantity = 3d;

    public final static double defaultTicketDiscount = 0.2d;

    public double controlTotalTaxCost = 1.00095d;
    public double controlProductTaxCost = 0.33365d;


    public final static double[][] control_product =
            {
                    //None
                    {
                            //includeTax
                            10d,
                            //ExcludeTax
                            9.47867d
                    },

                    //DISC_P
                    {
                            //includeTax
                            8d,
                            //ExcludeTax
                            7.58294d
                    },

                    //DISC
                    {
                            //includeTax
                            6.40d,
                            //ExcludeTax
                            6.06635d
                    }
            };

    public final static double[][] control_total =
            {
                    //None
                    {
                            //includeTax
                            30d,
                            //ExcludeTax
                            28.43601d
                    },

                    //DISC_P
                    {
                            //includeTax
                            24d,
                            //ExcludeTax
                            22.74882d
                    },

                    //DISC
                    {
                            //includeTax
                            19.2d,
                            //ExcludeTax
                            18.19905d
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
        TicketLine result = new TicketLine(product, quantity, area);
        result.setCustomPrice(price);
        return result;
    }
}
