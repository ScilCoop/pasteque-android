package fr.pasteque.client.unit.models;

import fr.pasteque.client.models.Discount;
import org.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Test;

public class DiscountTest {
    
    @Test
    public void discountTest() {
        assertNotNull(new Discount("123123", 1, 1, 1, "12345", 2));
    }
    
    @Test
    public void toJSONTest() throws Exception {
        Discount disc = new Discount("myId", 20, 10, 50, "myBarCode", 1);
        JSONObject obj = disc.toJSON();
        System.out.println("result: " + obj);
    }
    
    
    // ---Pour permettre l'exécution des test----------------------                                                                                                                                                  
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
	return new junit.framework.JUnit4TestAdapter(DiscountTest.class);
    }

}
