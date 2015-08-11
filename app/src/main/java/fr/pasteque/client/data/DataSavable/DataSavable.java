package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import fr.pasteque.client.utils.exception.DataSavableException;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */
public interface DataSavable {
    public void save(Context ctx) throws DataSavableException;
    public void load(Context ctx) throws DataSavableException;
}
