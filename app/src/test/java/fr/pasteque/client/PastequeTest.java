package fr.pasteque.client;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class PastequeTest {

    @Test
    public void testGetUniversalLog() throws Exception {
        assertEquals("Pasteque:PastequeTest:testGetUniversalLog", Pasteque.getUniversalLog());
    }

    @Test
    public void testRemovePackageNoIndex() throws Exception {
        String expected = "cannotFindIndex ";
        assertEquals(Pasteque.removePackage(expected), expected);
    }
}