package fr.pasteque.client.activities;

import android.os.Bundle;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEventListener;
import fr.pasteque.client.utils.DefaultPosDeviceTask;
import fr.pasteque.client.utils.PosDeviceTask;

/**
 * Activity to manage connected devices
 * Manage connection/disconnection in the activity lifecycle
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSConnectedTrackedActivity extends TrackedActivity implements DeviceManagerEventListener {

    public enum State {
        OnStart,
        OnResume,
        OnPause,
        OnDestroy
    }

    //Thread safety area
    private POSDeviceManager posConnectedManager;
    private DeviceManagerInThread deviceManagerInThread;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        this.posConnectedManager = POSDeviceManager.createPosConnection();
        deviceManagerInThread = new DeviceManagerInThread(posConnectedManager);
        this.posConnectedManager.setEventListener(this);
    }

    private void askAndConnect(State state) {
        if (posConnectedManager.shouldConnect(state)) {
            //Should not be in thread because creates a Handler in retro-compatibilities
            posConnectedManager.connectDevice();
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
        this.posConnectedManager.setEventListener(null);
    }

    public boolean isPrinterConnected() {
        return posConnectedManager.isPrinterConnected();
    }

    public DeviceManagerInThread getDeviceManagerInThread() {
        return deviceManagerInThread;
    }


    protected static class DeviceManagerInThread {

        public interface Task extends PosDeviceTask.SynchronizedTask {
        }

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
                public void execute(POSDeviceManager manager) {
                    manager.connectDevice();
                }
            });
        }

        public void disconnect() {
            new DefaultPosDeviceTask(deviceManager).execute(new DefaultPosDeviceTask.DefaultSynchronizedTask() {
                @Override
                public void execute(POSDeviceManager manager) {
                    manager.disconnectDevice();
                }
            });
        }
    }
}
