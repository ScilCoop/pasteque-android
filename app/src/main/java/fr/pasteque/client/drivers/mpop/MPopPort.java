package fr.pasteque.client.drivers.mpop;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import fr.pasteque.client.drivers.mpop.MPopEntry;

import java.util.ArrayList;

/**
 * Created by svirch_n on 21/12/15.
 */
public abstract class MPopPort extends StarIOPort{

    public static MPopEntries searchPrinterEntry() {
        MPopEntries mPopEntries = new MPopEntries();
        try {
            ArrayList<PortInfo> ports = StarIOPort.searchPrinter("BT:");
            for (PortInfo each: ports) {
                mPopEntries.add(each.getPortName(), each.getMacAddress());
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
        return mPopEntries;
    }
}
