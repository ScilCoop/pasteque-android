package fr.pasteque.client.drivers;

import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.mpop.MPopDeviceManager;
import fr.pasteque.client.utils.PastequeConfiguration;

/**
 * Class
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSDeviceManager {
    public static POSDeviceManager createPosConnection() {
        switch (Pasteque.getConfiguration().getPrinterDriver()) {
            case PastequeConfiguration.PrinterDriver.STARMPOP:
                return new MPopDeviceManager();
            default:
                return new DefaultDeviceManager();
        }
    }

    public abstract boolean connect();

    public abstract boolean disconnect();
}
