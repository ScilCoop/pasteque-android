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
public abstract class AbstractObjectDataSavable extends AbstractDataSavable {

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
