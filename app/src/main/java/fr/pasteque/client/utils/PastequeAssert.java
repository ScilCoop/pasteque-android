package fr.pasteque.client.utils;

import fr.pasteque.client.BuildConfig;

/**
 * Created by nsvir on 30/07/15.
 * n.svirchevsky@gmail.com
 */
public class PastequeAssert {

    public static void assertTrue(boolean value) {
        if (BuildConfig.DEBUG && value) {
            throw new RuntimeException();
        }
    }

    public static void assertFalse(boolean value) {
        if (BuildConfig.DEBUG && !value) {
            throw new RuntimeException();
        }
    }

    public static void runtimeException() {
        if (BuildConfig.DEBUG)
            throw new RuntimeException();
    }
}
