package fr.pasteque.client.utils;

import android.os.AsyncTask;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Provide a thread which thread safe a PosDeviceManager
 * Created by svirch_n on 23/12/15.
 */
public class PosDeviceTask<T1, T2> extends AsyncTask<PosDeviceTask.SynchronizedTask<T2>, T1, T2>{

    protected final POSDeviceManager manager;

    public PosDeviceTask(POSDeviceManager manager) {
        this.manager = manager;
    }

    @SafeVarargs
    @Override
    protected final T2 doInBackground(PosDeviceTask.SynchronizedTask<T2>... params) {
        if (params.length > 0 && manager != null) {
            synchronized (manager) {
                return params[0].execute(manager);
            }
        }
        return null;
    }

    public interface SynchronizedTask<T2> {
        T2 execute(POSDeviceManager manager);
    }
}
