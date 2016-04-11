package fr.pasteque.client;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;

/**
 * Created by nsvir on 08/08/15.
 * n.svirchevsky@gmail.com
 */

@RunWith(PowerMockRunner.class)
public class ConfigureTest {

    private Context fakeContext;

    @Before
    public void setup() {
        this.fakeContext = createMock(Context.class);
    }

    @Test
    public void isAccountValidTest() {
        //TODO
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ConfigureTest.class);
    }
}
