package fr.pasteque.client.activities;

import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.utils.DefaultPosDeviceTask;
import fr.pasteque.client.utils.PosDeviceTask;

/**
 * Activity to manage connected devices
 * Manage connection/disconnection in the activity lifecycle
 * Created by svirch_n on 23/12/15.
 */
public class POSConnectedTrackedActivity extends TrackedActivity {

    public enum State {
        OnStart,
        OnResume,
        OnPause,
        OnDestroy
    }

    //Thread safety area
    private final POSDeviceManager posConnectedManager;
    private final DeviceManagerInThread deviceManagerInThread;

    public POSConnectedTrackedActivity() {
        this.posConnectedManager = POSDeviceManager.createPosConnection();
        deviceManagerInThread = new DeviceManagerInThread(posConnectedManager);
    }

    private void askAndConnect(State state) {
        if (posConnectedManager.shouldConnect(state)) {
            deviceManagerInThread.connect();
        }
    }

    private void askAndDisconnect(State state) {
        if (posConnectedManager.shouldDisconnect(state)) {
            deviceManagerInThread.disconnect();
        }
    }

    private void askAndManageConnection(State state) {
        askAndConnect(state);
        askAndDisconnect(state);
    }

    @Override
    protected void onStart() {
        super.onStart();
        askAndManageConnection(State.OnStart);
    }

    @Override
    public void onResume() {
        super.onResume();
        askAndManageConnection(State.OnResume);
    }

    @Override
    public void onPause() {
        super.onPause();
        askAndManageConnection(State.OnPause);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        askAndManageConnection(State.OnDestroy);
    }

    public DeviceManagerInThread getDeviceManagerInThread() {
        return deviceManagerInThread;
    }


    protected static class DeviceManagerInThread {

        public interface Task extends PosDeviceTask.SynchronizedTask<Void> {}

        private final POSDeviceManager deviceManager;

        public DeviceManagerInThread(POSDeviceManager deviceManager) {
            this.deviceManager = deviceManager;
        }

        public void execute(final Task task) {
            //noinspection unchecked
            new PosDeviceTask<Void, Void>(deviceManager).execute(task);
        }

        public void connect() {
            new DefaultPosDeviceTask(deviceManager).execute(new DefaultPosDeviceTask.DefaultSynchronizedTask() {
                @Override
                public Boolean execute(POSDeviceManager manager) {
                    return manager.connect();
                }
            });
        }

        public void disconnect() {
            new DefaultPosDeviceTask(deviceManager).execute(new DefaultPosDeviceTask.DefaultSynchronizedTask() {
                @Override
                public Boolean execute(POSDeviceManager manager) {
                    return manager.disconnect();
                }
            });
        }
    }
}
