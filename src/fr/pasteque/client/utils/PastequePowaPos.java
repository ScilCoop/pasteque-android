package fr.pasteque.client.utils;

import android.content.Context;

import com.mpowa.android.sdk.powapos.PowaPOSSingleton;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;

/**
 *  Singleton to prevent multiple connection to Powa
 *  PowaPos sdk 1.14.0 has one built-in
 *  This one is thread safe thought
 *
 *  Will be useful in the future
 *  WIP
 */
public class PastequePowaPos extends PowaPOSSingleton {
    private static final String POWA_POS_SING_TAG = "PastequePowaPos";
    private static boolean mCreated = false;
    private static PastequePowaPos mInstance;

    // Protected to for use of getSingleton;
    protected PastequePowaPos() {
        super();
    }

    public synchronized static PastequePowaPos getSingleton() {
        if (mInstance == null) {
            mInstance = new PastequePowaPos();
        }
        return mInstance;
    }

    @Override
	public synchronized void create(Context context, PowaPOSCallback callback) {
        super.create(context, callback);
        mCreated = true;
    }

    @Override
	public synchronized void dispose() {
        super.dispose();
        mCreated = false;
    }
}
