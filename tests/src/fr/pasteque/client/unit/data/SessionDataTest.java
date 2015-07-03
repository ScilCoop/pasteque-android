/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.unit.data;

import android.content.Context;
import android.test.mock.MockContext;
import fr.pasteque.client.data.DiscountData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Session;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author nsvir
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionDataTest {

    public static final String TMP_FILENAME = "tmp/session.data";
    public static final String FILENAME = "session.data";
    
    @Mock
    private Context fakeContext;
    private BufferedReader reader;
    private BufferedOutputStream fakeOutputStream;
    
    @Before
    public void setup() throws IOException {
        PipedInputStream pipeInput = new PipedInputStream();
        this.reader = new BufferedReader(new InputStreamReader(pipeInput));
        this.fakeOutputStream = new BufferedOutputStream(new PipedOutputStream(pipeInput));
    }
    
    @Test
    public void currentSessionTest() throws FileNotFoundException {
        Context context = mock(Context.class);
        when(context.openFileInput(FILENAME)).thenReturn(new FileInputStream(TMP_FILENAME));
        SessionData.currentSession(context);
    }
    
    @Test
    public void saveTest() throws FileNotFoundException, Exception {
        when(fakeContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)).thenReturn(new FileOutputStream(TMP_FILENAME));
        SessionData.saveSession(fakeContext);
        
        SessionData.newSessionIfEmpty();
        Session session = SessionData.currentSession(fakeContext);
        session.newTicket();
        
        when(fakeContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)).thenReturn(new FileOutputStream(TMP_FILENAME));
        SessionData.saveSession(fakeContext);
        
        when(fakeContext.openFileInput(FILENAME)).thenReturn(new FileInputStream(TMP_FILENAME));
        SessionData.loadSession(fakeContext);
    }
}
