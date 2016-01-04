package fr.pasteque.client.drivers;

import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Classed used to
 * Created by svirch_n on 23/12/15.
 */
public class DefaultDeviceManager extends POSDeviceManager {
    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }
}
