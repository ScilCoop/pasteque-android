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

    private static final String sorry = "Sorry, I am not good enough, yet, to detect your issue";
    private static final String warning = "Warning: ";
    private static final String proposition = "Proposition: ";

    public enum Status {
        FILE_NOT_FOUND,
        CLASS_NOT_FOUND
    }

    public enum Action {
        LOADING,
        SAVING
    }

    public Status status;
    public Throwable Exception;

    private Action action;
    private String fileName;
    private int index = -1;
    private List<? extends Object> list;
    private String fileContent;

    public DataCorruptedException(Throwable e, Action action) {
        this.Exception = e;
        this.action = action;
        if (e instanceof FileNotFoundException) {
            this.status = Status.FILE_NOT_FOUND;
        } else {
            this.status = Status.CLASS_NOT_FOUND;
        }
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

    public DataCorruptedException addFileContent(String stringFile) {
        this.fileContent = stringFile;
        return this;
    }

    private String loadingInspection() {
        String result = "Loading, ";

        if (list != null) {
            result += "ListSizeExpected: " + list.size() + " ";
        }
        result += "LoadedObjects: " + index + " ";
        if (list == null || index < 0) {
            result += proposition + "You may want to re-upload the data or uninstall the application";
        } else if (index >= list.size()) {
            result += warning + "getNumberOfObjects asks more objects than getObjectList gave";
        } else {
            result += "Object concerned: " + list.get(index).getClass().getName();
            result += proposition + "Does the concerned object is Serializable ?";
        }
        return result;
    }

    private String savingInspection() {
        return "Saving, " + sorry;
    }

    private String inspection() {
        String result = "";
        if (status == Status.FILE_NOT_FOUND) {
            return "File not found: '" + fileName + "'";
        }
        switch (action) {
            case LOADING:
                return loadingInspection();
            case SAVING:
                return savingInspection();
        }
        return sorry;
    }

    public String inspectError() {
        return inspection();
    }

}
