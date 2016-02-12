package fr.pasteque.client.activities.test;

import android.test.ActivityInstrumentationTestCase2;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.mpop.MPopEntries;
import fr.pasteque.client.drivers.mpop.MPopPort;
import fr.pasteque.client.utils.PastequeConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by svirch_n on 05/02/16
 * Last edited at 10:55.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Pasteque.class)
public class MPopActivityTest extends ActivityInstrumentationTestCase2<MPopActivity> {

    MPopActivity activity;

    private PastequeConfiguration fakeConfiguration;

    public MPopActivityTest() {
        super(MPopActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();


        fakeConfiguration = PowerMockito.mock(PastequeConfiguration.class);
        PowerMockito.when(fakeConfiguration.getPrinterDriver()).thenReturn(PastequeConfiguration.PrinterDriver.STARMPOP);
        MPopEntries mPopEntries = MPopPort.searchPrinterEntry();
        if (mPopEntries.size() != 1) {
            throw new RuntimeException("One (and only one) Mpop should be paired");
        }
        PowerMockito.when(fakeConfiguration.getPrinterAddress()).thenReturn(mPopEntries.getEntries()[0].toString());

        PowerMockito.mockStatic(Pasteque.class);
        PowerMockito.when(Pasteque.getConf()).thenReturn(this.fakeConfiguration);
    }



    @Test
    public void testHasActivity() {
        assertNotNull(activity);
    }

    @Test
    public void testHasPrinterAddress() {
        assertNotNull(fakeConfiguration.getPrinterAddress());
    }

    public void testIsMPopPrinter() {
        assertEquals(PastequeConfiguration.PrinterDriver.STARMPOP, fakeConfiguration.getPrinterDriver());
    }
}