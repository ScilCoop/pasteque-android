package fr.pasteque.client;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import fr.pasteque.client.drivers.DefaultDeviceManager;
import fr.pasteque.client.utils.PastequeConfiguration;

/**
 * Created by nsvir on 21/09/15.
 * n.svirchevsky@gmail.com
 */
public class Pasteque extends Application {

    public static final String TAG = "Pasteque:";
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Pasteque.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Pasteque.context;
    }

    public static String getStringResource(int resourceId) {
        return Pasteque.getAppContext().getString(resourceId);
    }

    public static PastequeConfiguration getConfiguration() {
        return new PastequeConfiguration(getAppContext());
    }

    //shorter function
    public static PastequeConfiguration getConf() {
        return getConfiguration();
    }

    public static StackTraceElement getStackTraceElement() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Pasteque.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                return ste;
            }
        }
        return null;
    }

    public static String getUniversalLog() {
        StackTraceElement stackTraceElement = getStackTraceElement();
        if (stackTraceElement != null) {
            return "Pasteque:" + removePackage(stackTraceElement.getClassName()) + ":" + stackTraceElement.getMethodName();
        } else {
            return "Pasteque: <could not get stacktrace>";
        }
    }

    protected static String removePackage(String className) {
        int index = className.lastIndexOf(".") + 1;
        if (index != -1) {
           return className.substring(index);
        }
        return className;
    }

    public static void log(String message) {
        Log.d(getUniversalLog(), message);
    }
}
