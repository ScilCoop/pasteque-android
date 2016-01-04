package fr.pasteque.client.drivers.mpop;

import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Created by svirch_n on 23/12/15.
 */
public class MPopDeviceManager extends POSDeviceManager {

    StarIoExtManager manager;

    public MPopDeviceManager() {
        manager = new StarIoExtManager(StarIoExtManager.Type.Standard, Pasteque.getConfiguration().getPrinterModel(), "", 10000, Pasteque.getAppContext());
    }

    @Override
    public boolean connect() {
        return this.manager.connect();
    }

    @Override
    public boolean disconnect() {
        return this.manager.disconnect();
    }
}
