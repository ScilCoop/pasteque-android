package fr.pasteque.client.utils;

import fr.pasteque.client.BuildConfig;

/**
 * Created by nsvir on 30/07/15.
 * n.svirchevsky@gmail.com
 */
public class Assert {

    public void assertTrue(boolean value) {
        if (BuildConfig.DEBUG && value) {
            throw new RuntimeException();
        }
    }

    public void assertFalse(boolean value) {
        if (BuildConfig.DEBUG && !value) {
            throw new RuntimeException();
        }
    }
}
