package fr.pasteque.client.utils.file;

import android.content.Context;
import fr.pasteque.client.Pasteque;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by nsvir on 19/10/15.
 * n.svirchevsky@gmail.com
 */
public class InternalFile extends File {

    private String fileName;

    public InternalFile(String fileName) {
        this.fileName = fileName;
    }

    public InternalFile() {
        this.fileName = null;
    }

    protected FileInputStream openRead() throws FileNotFoundException {
        return Pasteque.getAppContext().openFileInput(this.fileName);
    }

    protected FileOutputStream openWrite() throws FileNotFoundException {
        return Pasteque.getAppContext().openFileOutput(this.fileName, Context.MODE_PRIVATE);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
