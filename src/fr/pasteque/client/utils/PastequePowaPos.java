package fr.pasteque.client.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.mpowa.android.sdk.common.base.PowaDriver;
import com.mpowa.android.sdk.common.base.PowaEnums;
import com.mpowa.android.sdk.powapos.PowaPOSSingleton;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallbackInt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Singleton to prevent multiple connection to Powa
 * PowaPos sdk 1.14.0 has one built-in
 * <p/>
 * Will be useful in the future
 * WIP
 */
public class PastequePowaPos extends PowaPOSSingleton {
    private static final String POWA_POS_SING_TAG = "PastequePowaPos";
    private static boolean mCreated = false;
    private static PastequePowaPos mInstance;

    protected List<Pair<String, PowaPOSCallback>> mPeripheralExternalEvents;

    // Protected to for use of getSingleton;
    protected PastequePowaPos() {
        super();
        mPeripheralExternalEvents = new LinkedList<>();
        this.peripheralInternalEvents = new PowaPOSCallbackInt("PastequePowaPosListener") {
            public void onAlert(final String id, final String data) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onAlert(id, data);
                            }
                        }
                    });
                }

            }

            public void onMCUBootloaderUpdateFailed(final PowaPOSEnums.BootloaderUpdateError error) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUBootloaderUpdateFailed(error);
                            }
                        }
                    });
                }

            }

            public void onMCUBootloaderUpdateStarted() {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUBootloaderUpdateStarted();
                            }
                        }
                    });
                }

            }

            public void onMCUBootloaderUpdateProgress(final int progress) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUBootloaderUpdateProgress(progress);
                            }
                        }
                    });
                }

            }

            public void onMCUBootloaderUpdateFinished() {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUBootloaderUpdateFinished();
                            }
                        }
                    });
                }

            }

            public void onUSBReceivedData(final PowaPOSEnums.PowaUSBCOMPort port, final byte[] data) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onUSBReceivedData(port, data);
                            }
                        }
                    });
                }

            }

            public void onMCUInitialized(final PowaPOSEnums.InitializedResult result) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUInitialized(result);
                            }
                        }
                    });
                }

            }

            public void onMCUConnectionStateChanged(final PowaEnums.ConnectionState newState) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUConnectionStateChanged(newState);
                            }
                        }
                    });
                }

            }

            public void onMCUFirmwareUpdateStarted() {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUFirmwareUpdateStarted();
                            }
                        }
                    });
                }

            }

            public void onMCUFirmwareUpdateProgress(final int progress) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUFirmwareUpdateProgress(progress);
                            }
                        }
                    });
                }

            }

            public void onMCUFirmwareUpdateFinished() {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUFirmwareUpdateFinished();
                            }
                        }
                    });
                }

            }

            public void onCashDrawerStatus(final PowaPOSEnums.CashDrawerStatus status) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onCashDrawerStatus(status);
                            }
                        }
                    });
                }

            }

            public void onRotationSensorStatus(final PowaPOSEnums.RotationSensorStatus status) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onRotationSensorStatus(status);
                            }
                        }
                    });
                }

            }

            public void onScannerInitialized(final PowaPOSEnums.InitializedResult result) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onScannerInitialized(result);
                            }
                        }
                    });
                }

            }

            public void onPrintJobResult(final PowaPOSEnums.PrintJobResult result) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onPrintJobResult(result);
                            }
                        }
                    });
                }

            }

            public void onPrinterOutOfPaper() {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onPrinterOutOfPaper();
                            }
                        }
                    });
                }

            }

            public void onUSBDeviceAttached(final PowaPOSEnums.PowaUSBCOMPort type) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onUSBDeviceAttached(type);
                            }
                        }
                    });
                }

            }

            public void onUSBDeviceDetached(final PowaPOSEnums.PowaUSBCOMPort type) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onUSBDeviceDetached(type);
                            }
                        }
                    });
                }

            }

            public void onMCUSystemConfiguration(final Map<String, String> configuration) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onMCUSystemConfiguration(configuration);
                            }
                        }
                    });
                }

            }

            public void onScannerRead(final String data) {
                if (PastequePowaPos.this.mPeripheralExternalEvents != null) {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            for (Pair<String, PowaPOSCallback> cb : PastequePowaPos.this.mPeripheralExternalEvents) {
                                cb.second.onScannerRead(data);
                            }
                        }
                    });
                }

            }
        };
    }

    public synchronized static PastequePowaPos getSingleton() {
        if (mInstance == null) {
            mInstance = new PastequePowaPos();
        }
        return mInstance;
    }

    @Override
    public void create(Context context, PowaPOSCallback callback) {
        String msg = "Please use void create(Context context, PowaPOSCallback callback, String tag)";
        throw new UnsupportedOperationException(msg);
    }

    public synchronized void create(Context context, PowaPOSCallback callback, String tag) {
        super.create(context, callback);
        if (!mCreated) {
            mCreated = true;
            this.peripheralExternalEvents = null;
            if (callback != null) mPeripheralExternalEvents.add(new Pair<>(tag, callback));
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        mCreated = false;
    }

    public void addCallback(String tag, PowaPOSCallback callback) {
        mPeripheralExternalEvents.add(new Pair<>(tag, callback));
    }

    /**
     * Removes all callback with the same tag.
     * @param tag is the associated tag.
     */
    public void removeCallback(String tag) {
        Iterator<Pair<String, PowaPOSCallback>> it = mPeripheralExternalEvents.iterator();
        while (it.hasNext()) {
            Pair<String, PowaPOSCallback> item = it.next();
            if (item.first.equals(tag)) {
                it.remove();
            }
        }
    }

    /**
     * Removes specific callback.
     * @param callback is the reference.
     */
    public void removeCallback(PowaPOSCallback callback) {
        int max = mPeripheralExternalEvents.size();
        for (int pos = 0; pos < max; ++pos) {
            if (mPeripheralExternalEvents.get(pos).second == callback) {
                mPeripheralExternalEvents.remove(pos);
                break;
            }
        }
    }
}
