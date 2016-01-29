package fr.pasteque.client.utils;

import android.os.AsyncTask;
import fr.pasteque.client.drivers.POSDeviceManager;

/**
 * Provide a thread which thread safe a PosDeviceManager
 * Created by svirch_n on 23/12/15.
 */
public class PosDeviceTask<T1, T2> extends AsyncTask<PosDeviceTask.SynchronizedTask, T1, Boolean> {

    protected final POSDeviceManager manager;
    protected Exception exceptionRaised;
    protected OnFailure onFailure;
    protected OnSucess onSucess;

    public PosDeviceTask(POSDeviceManager manager) {
        this.manager = manager;
    }

    public PosDeviceTask(POSDeviceManager manager, OnSucess onSucess) {
        this.manager = manager;
        this.onSucess = onSucess;
    }

    public PosDeviceTask(POSDeviceManager manager, OnSucess onSucess, OnFailure onFailure) {
        this.manager = manager;
        this.onSucess = onSucess;
        this.onFailure = onFailure;
    }

    public PosDeviceTask(POSDeviceManager manager, OnFailure onFailure) {
        this.manager = manager;
        this.onFailure = onFailure;
    }

    @Override
    protected final Boolean doInBackground(PosDeviceTask.SynchronizedTask... params) {
        if (params.length > 0 && manager != null) {
            synchronized (manager) {
                try {
                    params[0].execute(manager);
                    return Boolean.TRUE;
                } catch (Exception exception) {
                    exceptionRaised = exception;
                }
            }
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean doInBackgroundSuccessful) {
        super.onPostExecute(doInBackgroundSuccessful);
        if (doInBackgroundSuccessful) {
            if (this.onSucess != null) {
                this.onSucess.execute();
            }
        } else {
            if (this.onFailure != null) {
                this.onFailure.execute();
            }
        }
    }

    public interface SynchronizedTask {
        void execute(POSDeviceManager manager) throws Exception;
    }

    public interface OnSucess {
        void execute();
    }

    public interface OnFailure {
        void execute();
    }
}
