package fr.pasteque.client.utils.file;

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
        this.filename = fileName;
    }

    /**
     * openRead is not used
     * @return null
     * @throws FileNotFoundException
     */
    @Override
    protected FileInputStream openRead() throws FileNotFoundException {
        return null;
    }

    @Override
    protected FileOutputStream openWrite() throws FileNotFoundException {
        java.io.File file = Pasteque.getAppContext().getExternalFilesDir(null);
        if (file == null) {
            throw new FileNotFoundException("No external document directory");
        } else {
            return new FileOutputStream(file.getAbsolutePath() + "/" + filename);
        }
    }
}