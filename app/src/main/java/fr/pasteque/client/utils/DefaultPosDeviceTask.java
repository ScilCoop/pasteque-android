package fr.pasteque.client.utils;

import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class DefaultPosDeviceTask extends PosDeviceTask<Void, Boolean> {
    public DefaultPosDeviceTask(POSDeviceManager manager) {
        super(manager);
    }

    public DefaultPosDeviceTask(POSDeviceManager manager, OnSucess onSucess, OnFailure onFailure) {
        super(manager, onSucess, onFailure);
    }

    public interface DefaultSynchronizedTask extends SynchronizedTask{}
}
