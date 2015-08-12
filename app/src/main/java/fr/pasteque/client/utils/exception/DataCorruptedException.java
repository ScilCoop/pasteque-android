package fr.pasteque.client.utils.exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by nsvir on 11/08/15.
 * n.svirchevsky@gmail.com
 */

/**
 * Exception used by DataSavable
 * Store usefull informations to get a specifique message error.
 */
public class DataCorruptedException extends Exception {

    public static final int FILE_NOT_FOUND = 0;
    public static final int CLASS_NOT_FOUND = 1;

    public int Status;
    public Throwable Exception;
    private String fileName;
    private int index = -1;
    private List<? extends Object> list;

    public DataCorruptedException(Throwable e) {
        this.Exception = e;
        this.Status = FILE_NOT_FOUND;
    }

    public DataCorruptedException addFileName(String filename) {
        this.fileName = filename;
        return this;
    }

    public DataCorruptedException addObjectIndex(int index) {
        this.index = index;
        return this;
    }

    public DataCorruptedException addObjectList(List<?> objs) {
        this.list = objs;
        return this;
    }


}
