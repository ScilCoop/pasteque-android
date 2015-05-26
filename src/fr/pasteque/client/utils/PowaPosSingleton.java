package fr.pasteque.client.utils;

import android.content.Context;

import java.util.ArrayList;

import com.mpowa.android.sdk.powapos.PowaPOS;
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
public class PowaPosSingleton extends PowaPOSSingleton {
    private static final String POWA_POS_SING_TAG = "PowaPosSingleton";
    private static boolean mCreated = false;
    private static PowaPosSingleton mInstance;

    // Protected to for use of getInstance;
    protected PowaPosSingleton() {
        super();
    }

    public synchronized static PowaPosSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new PowaPosSingleton();
        }
        return mInstance;
    }

    public synchronized void create(Context context, PowaPOSCallback callback) {
        super.create(context, callback);
        mCreated = true;
    }

    public synchronized void dispose() {
        super.dispose();
        mCreated = false;
    }
}
