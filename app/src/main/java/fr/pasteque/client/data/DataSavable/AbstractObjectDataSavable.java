package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import fr.pasteque.client.data.DataSavable.interfaces.DataSavable;
import fr.pasteque.client.utils.exception.DataCorruptedException;
import fr.pasteque.client.utils.exception.DataCorruptedException.Action;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */
@Deprecated
public abstract class AbstractObjectDataSavable implements DataSavable {

    private static final String LOG_TAG = "Pasteque/DataSavable";

    /**
     * @return the filename to save/read from.
     */
    abstract protected String getFileName();

    /**
     * This function is called in save.
     * It returns a list of serializable objects that need to be saved.
     * @return the list of objects you want to save
     */
    abstract protected List<Object> getObjectList();


    /**
     * Called to restore the full list of objects.
     * @return number of objects to read
     */
    abstract protected int getNumberOfObjects();

    /**
     * This function is called in load.
     * It send all the recovered object.
     * The list is the same then received from getObjectList
     * @param objs the list of read objects
     */
    abstract protected void recoverObjects(List<Object> objs) throws DataCorruptedException;

    /**
     * This function has been created for a simple load without errors, still it call printStackStrace()
     * Protected because it is not a good practice
     * @param ctx the application's context
     */
    protected final void loadNoMatterWhat(Context ctx) {
        try {
            this.load(ctx);
        } catch (DataCorruptedException e) {
            if (e.status != DataCorruptedException.Status.FILE_NOT_FOUND) {
                e.printStackTrace();
            }
        } catch (IOError e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when an exception is received
     * Default behavior of warning is to remain silent about any warning errors
     * @param e
     * @return <code>false</code> to remain silent
     */
    @Override
    public boolean onLoadingFailed(DataCorruptedException e) {
        return false;
    }

    /**
     * Called when a fatal error is received
     * Default behavior of errors is to ask the user to update datas.
     * @param e
     * @return <code>true</code> to ask the user to update datas.
     */
    @Override
    public boolean onLoadingError(IOError e) {
        return true;
    }

    public final void save(Context ctx) {
        save(ctx, getObjectList());
    }

    public final void load(Context ctx) throws DataCorruptedException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        List<Object> objs = new ArrayList<>();
        int i = 0;
        int objectsToRead = this.getNumberOfObjects();
        try {
            fis = ctx.openFileInput(getFileName());
            ois = new ObjectInputStream(fis);
            for (i = 0; i < objectsToRead; i++) {
                objs.add(ois.readObject());
            }
        } catch (ClassNotFoundException | FileNotFoundException e) {
            throw new DataCorruptedException(e, Action.LOADING)
                    .addFileName(getFileName())
                    .addObjectIndex(i)
                    .addObjectList(getObjectList());
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            close(ois);
        }
        this.recoverObjects(objs);
    }

    private void save(Context ctx, List<?> objs) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        int forError = 0;
        try {
            fos = ctx.openFileOutput(this.getFileName(), Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            for (Object obj : objs) {
                forError++;
                oos.writeObject(obj);
            }
            oos.flush();
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            this.close(oos);
        }
    }

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
