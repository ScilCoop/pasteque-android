/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.models;

import fr.pasteque.client.models.CashRegister;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author svirch_n
 */
public class CashRegisterTest {

    @Test
    public void cashRegisterTest() {
        assertNotNull(new CashRegister(1, "", "", 1));
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(CashRegisterTest.class);
    }
}
