package fr.pasteque.client.activities.test;

import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;

/**
 * Created by svirch_n on 05/02/16.
 */
public class MPopActivity extends POSConnectedTrackedActivity {

    @Override
    public boolean onDeviceManagerEvent(DeviceManagerEvent event) {
        return false;
    }
}
