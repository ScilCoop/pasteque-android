package fr.pasteque.client.drivers.mpop;

import android.content.Context;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.mpop.MPopEntry;

import java.util.ArrayList;

/**
 * Mostly static class that improve StarIOPort statics functions
 * Created by svirch_n on 21/12/15.
 */
public abstract class MPopPort {

    public static final int timeout = 10000; //10000ms

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

    static StarIOPort getPort(String portName) throws StarIOPortException {
        return getPort(portName, null, timeout, Pasteque.getAppContext());
    }

    static StarIOPort getPort(String portName, String portSettings, int timeout, Context context) throws StarIOPortException {
        return StarIOPort.getPort(portName, portSettings,timeout, context);
    }
}
