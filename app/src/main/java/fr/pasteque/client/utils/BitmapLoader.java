package fr.pasteque.client.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.utils.file.InternalFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by svirch_n on 26/02/16
 * Last edited at 15:55.
 */
public class BitmapLoader {

    public MapFifo<String, Bitmap> buffer = new MapFifo<>(Pasteque.getConf().getBitmapBufferSize());

    public Bitmap get(String imageDirectory, String filename) {
        return getBitmap(imageDirectory, filename);
    }

    private String createKey(String n1, String n2) {
        return n1 + ":" + n2;
    }

    private Bitmap getBitmap(String imageDirectory, String filename) {
        Bitmap result = buffer.get(createKey(imageDirectory, filename));
        if (result == null) {
            result = readBitmap(imageDirectory, filename);
        }
        return result;
    }

    private Bitmap readBitmap(String imageDirectory, String filename) {
        try {
            FileInputStream fis = getFileInputStream(imageDirectory, filename);
            Bitmap result = BitmapFactory.decodeStream(fis);
            buffer.put(createKey(imageDirectory, filename), result);
            return result;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private static FileInputStream getFileInputStream(String imageDirectory, String filename) throws FileNotFoundException {
        return new FileInputStream(new InternalFile(imageDirectory, filename));
    }

    private static class MapFifo<KEY, VALUE> {

        private int max_length;
        private int length;
        private LinkedList<KEY> history = new LinkedList<>();
        private Map<KEY, VALUE> content;

        public MapFifo(int capacity) {
            max_length = capacity;
            content = new HashMap<>(capacity);
        }

        public void put(KEY key, VALUE value) {
            if (buffIsFull()) {
                removeFirst();
            }
            length++;
            content.put(key, value);
            history.add(key);
        }

        public VALUE get(KEY key) {
            return content.get(key);
        }

        private void removeFirst() {
            KEY keyToRemove = history.getFirst();
            history.removeFirst();
            content.remove(keyToRemove);
            length--;
        }

        private boolean buffIsFull() {
            return length >= max_length;
        }

    }
}
