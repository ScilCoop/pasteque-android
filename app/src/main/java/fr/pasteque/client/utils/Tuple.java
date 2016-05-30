package fr.pasteque.client.utils;

import java.util.Map;

/**
 * Created by svirch_n on 30/05/16
 * Last edited at 11:21.
 */
public class Tuple<T, T1> extends java.util.AbstractMap.SimpleEntry<T,T1>{

    public Tuple(T first, T1 second) {
        super(first, second);
    }

    public T first() {
        return getKey();
    }

    public T1 second() {
        return getValue();
    }
}
