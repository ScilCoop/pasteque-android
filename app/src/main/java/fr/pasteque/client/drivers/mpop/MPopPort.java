package fr.pasteque.client.drivers.mpop;

import fr.pasteque.client.drivers.mpop.MPopEntry;

import java.util.ArrayList;

/**
 * Created by svirch_n on 21/12/15.
 */
public class MPopPort {


    public static MPopEntries searchPrinterEntry() {
        MPopEntries mPopEntries = new MPopEntries();
        mPopEntries.add(new MPopEntry("TestName", "TestValue"));
        return mPopEntries;
    }
}
