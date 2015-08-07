package fr.pasteque.client.models;

import android.content.Context;
import fr.pasteque.client.R;

/**
 * Created by nsvir on 07/08/15.
 * n.svirchevsky@gmail.com
 */
public class Version {

    private static String version;
    private static String level;

    public static void setVersion(String version, String level) {
        Version.version = version;
        Version.level = level;
    }

    public static boolean isValid(Context ctx) {
        return level != null &&
                level.equals(ctx.getResources().getString(R.string.level));
    }
}
