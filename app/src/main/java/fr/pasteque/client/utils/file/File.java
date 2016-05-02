package fr.pasteque.client.utils.file;

import android.content.Context;
import fr.pasteque.client.Pasteque;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Created by nsvir on 05/10/15.
 * n.svirchevsky@gmail.com
 */
public abstract class File extends java.io.File {

    public static final int DIRECTORY = 0;
    private Charset charset = Charsets.UTF_8;

    public File(java.io.File dir, String name) {
        super(dir, name);
    }

    public File(String path) {
        super(path);
    }

    public File(String dirPath, String name) {
        super(Pasteque.getAppContext().getDir(dirPath, Context.MODE_PRIVATE), name);
    }

    public File(String dirPath, int type) {
        super(Pasteque.getAppContext().getDir(dirPath, Context.MODE_PRIVATE).getAbsolutePath());

    }

    public File(URI uri) {
        super(uri);
    }

    public void write(String string) throws FileNotFoundException {
        FileOutputStream outputStream = null;
        try {
            outputStream = openWrite();
            writeString(string, outputStream);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public String read() throws FileNotFoundException {
        FileInputStream fis = null;
        String result = "";
        try {
            fis = openRead();
            result = readString(fis);
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return result;
    }

    private void writeString(String string, FileOutputStream outputStream) throws IOException {
        IOUtils.write(string.getBytes(this.charset), outputStream);
    }

    protected String readString(FileInputStream fis) {
        String result = null;
        try {
            byte[] bytes = IOUtils.toByteArray(fis);
            result = new String(bytes, this.charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public abstract FileInputStream openRead() throws FileNotFoundException;

    public abstract FileOutputStream openWrite() throws FileNotFoundException;
}
