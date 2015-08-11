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

    abstract protected String getFileName();

    abstract protected List<Object> getObjectList();

    abstract protected int getNumberOfObjects();

    abstract protected void saveObjects(List<Object> objs);

    public final void save(Context ctx) throws DataCorruptedException {
        save(ctx, getObjectList());
    }

    private void save(Context ctx, List<Object> objs) throws DataCorruptedException {
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
            new IOError(e);
        } finally {
            close(ois);
        }
    }
}
