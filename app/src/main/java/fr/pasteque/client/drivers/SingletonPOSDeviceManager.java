package fr.pasteque.client.drivers;

import fr.pasteque.client.activities.POSConnectedTrackedActivity;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public abstract class SingletonPOSDeviceManager extends POSDeviceManager {

    protected static POSDeviceManager singleton;
    protected static int connected;

    @Override
    public final boolean connect() {
        boolean result = true;
        if (connected++ == 0) {
            result = firstConnect(singleton);
        }
        result = result && intermediateConnect();
        return result;
    }

    protected abstract boolean intermediateConnect();

    protected abstract boolean intermediateDisconnect();

    @Override
    public final boolean disconnect() {
        boolean result = true;
        if (connected > 0 && --connected == 0) {
            result = lastDisconnect(singleton);
            result = result && intermediateDisconnect();
        }
        return result;
    }

    protected abstract boolean lastDisconnect(POSDeviceManager singleton);

    protected abstract boolean firstConnect(POSDeviceManager singleton);


    @Override
    public boolean shouldDisconnect(POSConnectedTrackedActivity.State state) {
        switch (state) {
            case OnDestroy:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean shouldConnect(POSConnectedTrackedActivity.State state) {
        switch (state) {
            case OnStart:
                return true;
            default:
                return false;
        }
    }

}
