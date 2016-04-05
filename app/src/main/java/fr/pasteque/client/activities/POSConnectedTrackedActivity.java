package fr.pasteque.client.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
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

    private final static int maxConnectionTries = Pasteque.getConf().getPrinterConnectTry();

    public enum State {
        OnStart,
        OnResume,
        OnPause,
        OnDestroy
    }

    //Thread safety area
    private POSDeviceManager posConnectedManager;
    private DeviceManagerInThread deviceManagerInThread;
    private int connectionTries;
    private POSDeviceFeaturesFragment fragment;

    /**
     * Bluetooth device Listener
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if (posConnectedManager != null) {
                    posConnectedManager.connectBluetooth();
                }
            }

        }
    };

    public final boolean deviceManagerHasCashDrawer() {
        return posConnectedManager.hasCashDrawer();
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        this.posConnectedManager = POSDeviceManager.createPosConnection();
        deviceManagerInThread = new DeviceManagerInThread(posConnectedManager);
        this.posConnectedManager.setEventListener(this);
        fragment = POSDeviceFeaturesFragment.newInstance();
        bluetoothConnectedHandler();
    }

    /**
     * Create filters to handle bluetooth device connected
     */
    private void bluetoothConnectedHandler() {
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);
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

    public void setFragment(POSDeviceFeaturesFragment fragment) {
        this.fragment = fragment;
    }

    protected abstract boolean onDeviceManagerEvent(DeviceManagerEvent event);

public interface POSHandler {
    void onDeviceManagerEvent(DeviceManagerEvent event);
}
}
