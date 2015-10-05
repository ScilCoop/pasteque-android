package fr.pasteque.client.utils;

import android.content.Context;
import fr.pasteque.client.Pasteque;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by nsvir on 05/10/15.
 * n.svirchevsky@gmail.com
 */
public class File {

    private String fileName;
    private Charset charset = Charsets.UTF_8;

    public File(String fileName) {
        this.fileName = fileName;
    }

    public void write(String string) {
        FileOutputStream outputStream = null;
        try {
            outputStream = openWrite();
            writeString(string, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public String read() {
        FileInputStream fis = null;
        String result = "";
        try {
            fis = openRead();
            result = readString(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return result;
    }

    private void writeString(String string, FileOutputStream outputStream) throws IOException {
        IOUtils.write(string.getBytes(this.charset), outputStream);
    }

    private String readString(FileInputStream fis) {
        String result = null;
        try {
            byte[] bytes = IOUtils.toByteArray(fis);
            result = new String(bytes, this.charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private FileInputStream openRead() throws FileNotFoundException {
        return Pasteque.getAppContext().openFileInput(this.fileName);
    }

    private FileOutputStream openWrite() throws FileNotFoundException {
        return Pasteque.getAppContext().openFileOutput(this.fileName, Context.MODE_PRIVATE);
    }

}
