package fr.pasteque.client.utils.exception;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */
public class DataSavableException extends Throwable {
    public DataSavableException(IOException e) {
        if (e instanceof FileNotFoundException) {
            //Log.i(LOG_TAG, "No payment modes file to load");
        } else {
            //Log.e(LOG_TAG, "Error while loading payment modes", ioe);
        }
    }
}
