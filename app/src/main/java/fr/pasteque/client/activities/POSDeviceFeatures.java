package fr.pasteque.client.activities;

import android.os.Bundle;
import fr.pasteque.client.R;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;

/**
 * Created by svirch_n on 11/03/16
 * Last edited at 16:27.
 */
public class POSDeviceFeatures extends POSConnectedTrackedActivity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.container);
        findViewById(R.id.container);
        if (state == null) {
            //If the activity is first created and not just rotated
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, getDeviceFeaturesFragment());
            ft.commit();
        }
    }

    @Override
    protected boolean onDeviceManagerEvent(DeviceManagerEvent event) {
        return false;
    }
}
