package fr.pasteque.client.utils.exception;

import java.io.FileNotFoundException;

/**
 * Created by nsvir on 19/10/15.
 * n.svirchevsky@gmail.com
 */
public class SaveArchiveException extends Throwable {
    public SaveArchiveException(FileNotFoundException e) {
    }
}
