package fr.pasteque.client.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by svirch_n on 09/06/16
 * Last edited at 16:34.
 */
public class PaymentTest extends JSONability {

    String model = 'salut';

    int amount;
    int given;

    @Test
    public void toJSON() throws Exception {
        PaymentMode mode = DataTest.aPaymentMode();
        Payment payment = new Payment(mode, amount, given);
        model = '{"id": "' + '' + '",' +
                '"type": ,' +
                '"amount": ,' +
                '"back": ' +
                '"currencyId": ,' +
                '"currencyAmount": }';
    }

}