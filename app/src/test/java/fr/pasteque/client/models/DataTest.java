package fr.pasteque.client.models;

import java.util.List;

/**
 * Created by svirch_n on 09/06/16
 * Last edited at 17:03.
 */
public class DataTest {


    @SuppressWarnings("ConstantConditions")
    public static PaymentMode aPaymentMode() {
        int id = 1;
        String code = "aCode";
        String label = "aLabel";
        String backlabel = "aBackLabel";
        int flags = 0;
        boolean hasImage = false;
        List<PaymentMode.Return> rules = null;
        boolean active = true;
        int dispOrder = 0;
        return new PaymentMode(id, code, label, backlabel, flags, hasImage, rules, active, dispOrder);
    }
}
