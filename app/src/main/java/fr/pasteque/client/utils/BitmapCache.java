package fr.pasteque.client.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by svirch_n on 30/05/16
 * Last edited at 12:33.
 */
public class BitmapCache extends LruCache<String, Bitmap> {
    public BitmapCache(int bitmapBufferSize) {
        super(bitmapBufferSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
    }
}
