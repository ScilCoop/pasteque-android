package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import fr.pasteque.client.utils.exception.DataCorruptedException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */
public abstract class AbstractDataSavable implements DataSavable {

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
    abstract protected void recoverObjects(List<Object> objs);

    /**
     * This function has been created for a simple load without errors, still it call printStackStrace()
     * Protected because it is not a good practice
     * @param ctx the application's context
     */
    protected final void loadNoMatterWhat(Context ctx) {
        try {
            this.load(ctx);
        } catch (IOError|DataCorruptedException e) {
            e.printStackTrace();
        }
    }

    public final void save(Context ctx) throws DataCorruptedException {
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
            throw new DataCorruptedException(e)
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

    private void save(Context ctx, List<?> objs) throws DataCorruptedException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = ctx.openFileOutput(this.getFileName(), Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            for (Object obj : objs) {
                oos.writeObject(obj);
            }
        } catch (FileNotFoundException e) {
            throw new DataCorruptedException(e)
                    .addFileName(getFileName());
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
