/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.Configure;
import fr.pasteque.client.Constant;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.data.DataSavable.SessionData;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.User;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;

/**
 * @author nsvir
 */
@RunWith(PowerMockRunner.class)
//Used in setupd to mock the static method Pasteque.getAppContext
@PrepareForTest(Pasteque.class)
public class SessionDataTest extends AbstractDataTest{

    public String getTmpFilename() {
        return "session.json";
    }

    private Product product;
    private SessionData sessionData = new SessionData();

    @Override
    public void setup() throws IOException {
        super.setup();
        this.product = new Product("1", "Ma salade", "0123232343", 24.5, "NoTaxe", 0, false, false, 0.2, false);
    }

    @Test
    public void stressTest() throws Exception {
        addDefaultFileOutputExpected();
        addDefaultFileInputExpected();
        replayContext();
        this.sessionData.newSessionIfEmpty();
        Session session = this.sessionData.currentSession(fakeContext);
        session.setUser(new User("id", "name", "password", "permission"));
        this.sessionData.save(fakeContext);

        for (int i = 0; i < 10; i++) {
            Data.Session.load(fakeContext);
            this.sessionData.save(fakeContext);
        }

    }

    @Test
    public void saveTest() throws Exception {
        addDefaultFileInputExpected();
        addDefaultFileOutputExpected();
        replayContext();
        sessionData.save(fakeContext);
        sessionData.newSessionIfEmpty();
        Session session = this.sessionData.currentSession(fakeContext);
        session.newTicket();
        session.getCurrentTicket().addProduct(this.product);

        this.sessionData.save(fakeContext);

        this.sessionData.load(fakeContext);
        session = this.sessionData.currentSession(fakeContext);

        TicketLine ticket = session.getCurrentTicket().getLineAt(0);
        Product product = ticket.getProduct();

        assertEquals(this.product, product);


    }
}
