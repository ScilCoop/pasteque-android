package fr.pasteque.client;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import fr.pasteque.client.drivers.DefaultDeviceManager;
import fr.pasteque.client.utils.PastequeConfiguration;

import java.util.ArrayList;

/**
 * Created by nsvir on 21/09/15.
 * n.svirchevsky@gmail.com
 */
public class Pasteque extends Application {

    public static final String TAG = "Pasteque:";
    private static Context context;

    public static float getRestaurantMapWidth() {
        return 630f;
    }

    public static float getRestaurantMapHeight() {
        return 500f;
    }

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

    public static class Toast {

        private static android.widget.Toast lastToast;

        private Toast() {
        }

        public static void show(String message) {
            show(message, android.widget.Toast.LENGTH_SHORT);
        }

        public static void show(int stringid) {
            show(Pasteque.getStringResource(stringid));
        }

        public static void show(String message, int length) {
            cancelLastToast();
            android.widget.Toast toast = android.widget.Toast.makeText(Pasteque.getAppContext(), message, length);
            toast.show();
            lastToast = toast;
        }

        public static void cancelLastToast() {
            if (lastToast != null) {
                lastToast.cancel();
                lastToast = null;
            }
        }
    }

    public static class Log {

        public static StackTraceElement getStackTraceElement() {
            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
            for (int i = 1; i < stElements.length; i++) {
                StackTraceElement ste = stElements[i];
                if (!ste.getClassName().equals(Pasteque.class.getName())
                        && ste.getClassName().indexOf("java.lang.Thread") != 0
                        && ste.getClassName().indexOf("fr.pasteque.client.Pasteque$Log") != 0) {
                    return ste;
                }
            }
            return null;
        }

        protected static String removePackage(String className) {
            int index = className.lastIndexOf(".") + 1;
            if (index != -1) {
                return className.substring(index);
            }
            return className;
        }

        public static String getUniversalLog() {
            StackTraceElement stackTraceElement = getStackTraceElement();
            if (stackTraceElement != null) {
                return "Pasteque:" + removePackage(stackTraceElement.getClassName()) + ":" + stackTraceElement.getMethodName();
            } else {
                return "Pasteque: <could not get stacktrace>";
            }
        }

        public static void w(String message) {
            android.util.Log.w(getUniversalLog(), message);
        }

        public static void w(String message, Throwable e) {
            android.util.Log.w(getUniversalLog(), message, e);
        }

        public static void d(String message) {
            android.util.Log.d(getUniversalLog(), message);
        }

        public static void d(String message, Throwable e) {
            android.util.Log.d(getUniversalLog(), message, e);
        }
    }


}
