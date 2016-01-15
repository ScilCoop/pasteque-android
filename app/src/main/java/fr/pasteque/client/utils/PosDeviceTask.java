package fr.pasteque.client.utils;

import android.os.AsyncTask;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Provide a thread which thread safe a PosDeviceManager
 * Created by svirch_n on 23/12/15.
 */
public class PosDeviceTask<T1, T2> extends AsyncTask<PosDeviceTask.SynchronizedTask, T1, T2>{

    protected final POSDeviceManager manager;

    public PosDeviceTask(POSDeviceManager manager) {
        this.manager = manager;
    }

    @Override
    protected final T2 doInBackground(PosDeviceTask.SynchronizedTask... params) {
        if (params.length > 0 && manager != null) {
            synchronized (manager) {
                params[0].execute(manager);
            }
        }
        return null;
    }

    public interface SynchronizedTask {
        void execute(POSDeviceManager manager);
    }
}
