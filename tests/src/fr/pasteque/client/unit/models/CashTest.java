
package fr.pasteque.client.unit.models;

import fr.pasteque.client.models.Cash;
import static org.junit.Assert.*;
import org.junit.Test;

public class CashTest {

    @Test
    public void cashTest() {
        assertNotNull(new Cash(1));
    }

    // ---Pour permettre l'exécution des test----------------------                                                                                                                                                  
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(CashTest.class);
    }
}