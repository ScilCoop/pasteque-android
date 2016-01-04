package fr.pasteque.client.utils;

import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class DefaultPosDeviceTask extends PosDeviceTask<Void, Boolean> {
    public DefaultPosDeviceTask(POSDeviceManager manager) {
        super(manager);
    }

    public interface DefaultSynchronizedTask extends SynchronizedTask<Boolean> {}
}
