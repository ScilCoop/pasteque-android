package fr.pasteque.client.drivers.utils;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by svirch_n on 22/01/16.
 */
public class DeviceManagerEvent {

    public final static int None = 0;
    public final static int BaseRotation = 1;
    public final static int ScannerReader = 2;
    public final static int PrintDone = 3;
    public final static int PrintError = 4;


    public final int what;
    private Object extra;


    public DeviceManagerEvent(int eventValue) {
        this.what = eventValue;
    }

    public DeviceManagerEvent(int eventValue, Bundle extra) {
        this.what = eventValue;
        this.extra = extra;
    }

    public DeviceManagerEvent(int eventValue, Object object) {
        this.what = eventValue;
        this.extra = object;
    }

    public Object getExtra() {
        return extra;
    }

    public String getString() {
        return (String) extra;
    }

    public boolean extraEquals(Object object) {
        if (this.extra != null) {
            return this.extra.equals(object);
        }
        return false;
    }
}
