package fr.pasteque.client.utils.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by svirch_n on 04/05/16
 * Last edited at 10:29.
 */
public class TestFile extends File {

    public TestFile(String path) {
        super(path);
    }

    public TestFile(String dirPath, String name) {
        super(dirPath, name);
    }

    public TestFile(java.io.File dir, String name) {
        super(dir, name);
    }

    @Override
    protected FileInputStream openRead() throws FileNotFoundException {
        return new FileInputStream(this);
    }

    @Override
    protected FileOutputStream openWrite() throws FileNotFoundException {
        return new FileOutputStream(this);
    }
}
