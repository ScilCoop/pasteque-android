package fr.pasteque.client.utils;

import android.os.AsyncTask;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Created by svirch_n on 23/12/15.
 */
public abstract class PosDeviceTask<T, T1, T2> extends AsyncTask<T, T1, T2>{

    protected POSDeviceManager manager;

    public PosDeviceTask(POSDeviceManager manager) {
        this.manager = manager;
    }
}
