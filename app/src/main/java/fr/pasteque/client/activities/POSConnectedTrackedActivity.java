package fr.pasteque.client.activities;

import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.utils.PosDeviceTask;

/**
 * Activity to manage connected devices
 * Manage connection/disconnection in the activity lifecycle
 * Created by svirch_n on 23/12/15.
 */
public class POSConnectedTrackedActivity extends TrackedActivity {

    //Thread safety area
    private POSDeviceManager posConnectedManager = POSDeviceManager.createPosConnection();

    @Override
    public void onResume() {
        super.onResume();
        startPosDeviceManager();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPosDeviceManager();
    }

    private void startPosDeviceManager() {
        new connectPosDevice(posConnectedManager).execute();
    }

    private void stopPosDeviceManager() {
        new disconnectPosDevice(posConnectedManager).execute();
    }

    public static class connectPosDevice extends PosDeviceTask<Void, Void, Boolean> {

        public connectPosDevice(POSDeviceManager manager) {
            super(manager);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result;
            synchronized (manager) {
                result = manager.connect();
            }
            return result;
        }
    }

    public static class disconnectPosDevice extends PosDeviceTask<Void, Void, Boolean> {

        public disconnectPosDevice(POSDeviceManager manager) {
            super(manager);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result;
            synchronized (manager) {
                result = manager.disconnect();
            }
            return result;
        }
    }

}
