/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.Constant;
import fr.pasteque.client.data.DataSavable.SessionData;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.User;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * @author nsvir
 */
@RunWith(PowerMockRunner.class)
public class SessionDataTest {

    public static final String TMP_FILENAME = Constant.BUILD_FOLDER + "session.data";
    public static final String TMP_FILENAME_EMPTY = Constant.BUILD_FOLDER + "empty.data";

    private Context fakeContext;
    private Product product;
    private SessionData session = new SessionData();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setup() throws IOException {
        this.fakeContext = createMock(Context.class);
        this.product = new Product("1", "Ma salade", "0123232343", 24.5, "NoTaxe", 0, false, false, 0.2, false);
        File file = new File(TMP_FILENAME_EMPTY);
        file.getParentFile().mkdirs();
        file.createNewFile();
        file = new File(TMP_FILENAME);
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    private void addDefaultFileInputExpected() throws FileNotFoundException {
        expect(fakeContext.openFileInput(anyObject(String.class))).andStubAnswer(new IAnswer<FileInputStream>() {
            @Override
            public FileInputStream answer() throws Throwable {
                return new FileInputStream(TMP_FILENAME);
            }
        });
    }

    private void addDefaultFileOutputExpected() throws FileNotFoundException {
        expect(fakeContext.openFileOutput(anyObject(String.class), anyInt())).andStubAnswer(new IAnswer<FileOutputStream>() {
            @Override
            public FileOutputStream answer() throws Throwable {
                return new FileOutputStream(TMP_FILENAME);
            }
        });
    }

    @Test
    public void currentSessionTest() throws FileNotFoundException {
        addDefaultFileInputExpected();
        replay(fakeContext);
        session.currentSession(fakeContext);
    }

    @Test
    public void stressTest() throws Exception {
        addDefaultFileOutputExpected();
        addDefaultFileInputExpected();
        replay(fakeContext);
        Session session = this.session.currentSession(fakeContext);
        session.setUser(new User("id", "name", "password", "permission"));
        this.session.save(fakeContext);

        for (int i = 0; i < 10; i++) {
            Data.Session.load(fakeContext);
            this.session.save(fakeContext);
        }

    }

    @Test
    public void saveTest() throws Exception {
        addDefaultFileInputExpected();
        addDefaultFileOutputExpected();
        replay(fakeContext);
        session.save(fakeContext);
        session.newSessionIfEmpty();
        Session session = this.session.currentSession(fakeContext);
        session.newTicket();
        session.getCurrentTicket().

        addProduct(this.product);

        this.session.save(fakeContext);

        this.session.load(fakeContext);
        session = this.session.currentSession(fakeContext);

        TicketLine ticket = session.getCurrentTicket().getLineAt(0);
        Product product = ticket.getProduct();

        assertEquals(this.product, product);


    }
}
