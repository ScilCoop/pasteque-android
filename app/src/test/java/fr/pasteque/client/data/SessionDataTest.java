/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.TicketLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createNiceMock;

/**
 *
 * @author nsvir
 */
@RunWith(PowerMockRunner.class)
public class SessionDataTest {

    public static final String TMP_FILENAME = "tmp/session.data";
    public static final String TMP_FILENAME_EMPTY = "tmp/empty.data";
    public static final String FILENAME = "session.data";
    
    private Context fakeContext;
    private Product product;
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setup() throws IOException {
        PipedInputStream pipeInput = new PipedInputStream();
        product = new Product("1", "Ma salade", "0123232343", 24.5, "NoTaxe", 0, false, false, 0.2, false);
        File file = new File(TMP_FILENAME_EMPTY);
        file.getParentFile().mkdir();
        file.createNewFile();
    }
    
    @Test
    public void currentSessionTest() throws FileNotFoundException {
        Context context = createNiceMock(Context.class);
        expect(context.openFileInput(FILENAME)).andStubReturn(new FileInputStream(TMP_FILENAME));
        SessionData.currentSession(context);
    }

    @Test
    public void loadEmptyTest() throws IOException {
        Context context = createNiceMock(Context.class);
        expect(context.openFileInput(FILENAME)).andStubReturn(new FileInputStream(TMP_FILENAME_EMPTY));
        SessionData.loadSession(context);
    }

    @Test
    public void saveTest() throws Exception {
        expect(fakeContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)).andStubReturn(new FileOutputStream(TMP_FILENAME));
        SessionData.saveSession(fakeContext);
        
        SessionData.newSessionIfEmpty();
        Session session = SessionData.currentSession(fakeContext);
        session.newTicket();
        session.getCurrentTicket().addProduct(this.product);

        SessionData.saveSession(fakeContext);
        
        SessionData.loadSession(fakeContext);
        session = SessionData.currentSession(fakeContext);

        TicketLine ticket = session.getCurrentTicket().getLineAt(0);
        Product product = ticket.getProduct();
        assertEquals(this.product, product);


    }
}
