package fr.pasteque.client.utils.file;

import android.content.Context;
import android.os.Environment;
import fr.pasteque.client.Pasteque;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import fr.pasteque.client.R;

/**
 * Created by nsvir on 19/10/15.
 * n.svirchevsky@gmail.com
 */
public class ExternalFile extends File {

    public ExternalFile(String fileName) {
        super(Pasteque.getAppContext().getExternalFilesDir(null), fileName);
    }

    public ExternalFile(String dir, String filename) {
        //noinspection ConstantConditions
        super(Pasteque.getAppContext().getExternalFilesDir(null), filename);
    }

    /**
     * openRead is not used
     *
     * @return null
     * @throws FileNotFoundException
     */
    @Override
    protected FileInputStream openRead() throws FileNotFoundException {
        return null;
    }

    @Override
    protected FileOutputStream openWrite() throws FileNotFoundException {
        return new FileOutputStream(this);
    }
}