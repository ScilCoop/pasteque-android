package fr.pasteque.client.utils;

import fr.pasteque.client.BuildConfig;

/**
 * Created by nsvir on 30/07/15.
 * n.svirchevsky@gmail.com
 */
public class PastequeAssert {

    //private static boolean debug = BuildConfig.DEBUG;
    private static boolean debug = false;

    public static void assertTrue(boolean value) {
        if (debug && value) {
            throw new RuntimeException();
        }
    }

    public static void assertFalse(boolean value) {
        if (debug && !value) {
            throw new RuntimeException();
        }
    }

    public static void runtimeException() {
        if (debug)
            throw new RuntimeException();
    }
}
