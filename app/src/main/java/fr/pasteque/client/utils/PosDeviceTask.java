package fr.pasteque.client.utils;

import android.os.AsyncTask;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Provide a thread which thread safe a PosDeviceManager
 * Created by svirch_n on 23/12/15.
 */
public class PosDeviceTask<T1> extends AsyncTask<PosDeviceTask.SynchronizedTask, T1, Boolean> {

    protected final POSDeviceManager manager;
    protected Exception exceptionRaised;
    private SynchronizedTask synchronizedTask;

    public PosDeviceTask(POSDeviceManager manager) {
        this.manager = manager;
    }

    @Override
    protected final Boolean doInBackground(PosDeviceTask.SynchronizedTask... params) {
        if (Pasteque.getConf().isPrinterThreadAPriority()) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        } else {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        }
        if (params.length > 0 && manager != null) {
                try {
                    synchronizedTask = params[0];
                    synchronizedTask.execute(manager);
                    return Boolean.TRUE;
                } catch (Exception exception) {
                    exceptionRaised = exception;
                }

        }
        return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean doInBackgroundSuccessful) {
        super.onPostExecute(doInBackgroundSuccessful);
        synchronizedTask.result(doInBackgroundSuccessful, exceptionRaised);
    }

    public static abstract class SynchronizedTask {
        public abstract void execute(POSDeviceManager manager) throws Exception;
        public void result(boolean isSuccess, Exception exceptionRaised) {}
    }

}
