package fr.pasteque.client.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.drivers.utils.DeviceManagerEventListener;
import fr.pasteque.client.utils.DefaultPosDeviceTask;
import fr.pasteque.client.utils.PosDeviceTask;

import java.io.Serializable;

/**
 * Activity to manage connected devices
 * Manage connection/disconnection in the activity lifecycle
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSConnectedTrackedActivity extends TrackedActivity implements DeviceManagerEventListener, Serializable {

    public enum State {
        OnStart,
        OnResume,
        OnPause,
        OnDestroy
    }

    //Thread safety area
    private POSDeviceManager posConnectedManager;
    private DeviceManagerInThread deviceManagerInThread;
    private final static int maxConnectionTries = Pasteque.getConf().getPrinterConnectTry();
    private int connectionTries;
    private POSDeviceFeaturesFragment fragment;

    public final boolean deviceManagerHasCashDrawer() {
        return posConnectedManager.hasCashDrawer();
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        this.posConnectedManager = POSDeviceManager.createPosConnection();
        deviceManagerInThread = new DeviceManagerInThread(posConnectedManager);
        this.posConnectedManager.setEventListener(this);
        fragment = POSDeviceFeaturesFragment.newInstance(this);
    }

    public POSDeviceFeaturesFragment getDeviceFeaturesFragment() {
        return fragment;
    }

    private void askAndConnect(State state) {
        if (posConnectedManager.shouldConnect(state)) {
            deviceManagerInThread.connect();
            this.posConnectedManager.setEventListener(this);
        }
    }

    private void askAndDisconnect(State state) {
        if (posConnectedManager.shouldDisconnect(state)) {
            deviceManagerInThread.disconnect();
            this.posConnectedManager.setEventListener(null);
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

    public boolean isPrinterConnected() {
        return posConnectedManager.isPrinterConnected();
    }

    public DeviceManagerInThread getDeviceManagerInThread() {
        return deviceManagerInThread;
    }


    protected static class DeviceManagerInThread {

        public static abstract class Task extends PosDeviceTask.SynchronizedTask {
        }

        private final POSDeviceManager deviceManager;

        public DeviceManagerInThread(POSDeviceManager deviceManager) {
            this.deviceManager = deviceManager;
        }

        public void execute(final Task task) {
            //noinspection unchecked
            new PosDeviceTask<Void>(deviceManager).execute(task);
        }


        public void connect() {
            new DefaultPosDeviceTask(deviceManager).execute(new DefaultPosDeviceTask.DefaultSynchronizedTask() {
                public void execute(POSDeviceManager manager) throws Exception {
                    manager.connectDevice();
                }
            });
        }
        public void disconnect() {
            new DefaultPosDeviceTask(deviceManager).execute(new DefaultPosDeviceTask.DefaultSynchronizedTask() {
                public void execute(POSDeviceManager manager) throws Exception {
                    manager.disconnectDevice();
                }
            });
        }
    }

    @Override
    public boolean onThreadedDeviceManagerEvent(final DeviceManagerEvent event) {
        switch (event.what) {
            case DeviceManagerEvent.PrinterConnected:
                getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
                    @Override
                    public void execute(POSDeviceManager manager) throws Exception {
                        manager.printQueued();
                    }
                });
                break;
            case DeviceManagerEvent.DeviceConnectFailure:
                connectionTries++;
                if (connectionTries < maxConnectionTries) {
                    getDeviceManagerInThread().connect();
                }
                break;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onDeviceManagerEvent(event);
                fragment.onDeviceManagerEvent(event);
            }
        });
        return true;
    }

    protected abstract boolean onDeviceManagerEvent(DeviceManagerEvent event);

    public interface POSHandler {
        void onDeviceManagerEvent(DeviceManagerEvent event);
    }
}
