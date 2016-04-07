package fr.pasteque.client.viewer;

import android.app.Application;
import android.content.Context;

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
    public static PastequeConfiguration getConf() {return getConfiguration();}
}
