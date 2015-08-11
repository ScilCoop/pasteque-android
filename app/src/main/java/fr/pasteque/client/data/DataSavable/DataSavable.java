package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import fr.pasteque.client.utils.exception.DataCorruptedException;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */

/**
 * Interface used by Data
 * Classes that implements this interface have to use a decent system to save and load Objects.
 * Take a look at AbastractDataSavable to get some inspiration.
 *
 * Save/Load the object
 * @param ctx the application's context
 * @throws DataCorruptedException if the data cannot be write and the user can do something about it.
 * @throws IOError if the IO methodes throw an unexpected exception.
 */
public interface DataSavable {

    public void save(Context ctx) throws DataCorruptedException, IOError;
    public void load(Context ctx) throws DataCorruptedException, IOError;
}
