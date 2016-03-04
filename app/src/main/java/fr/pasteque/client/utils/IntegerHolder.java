package fr.pasteque.client.utils;

/**
 * Created by svirch_n on 04/03/16
 * Last edited at 17:28.
 */
public class IntegerHolder {
    public int value = 0;

    public void decrease() {
        value--;
    }

    public void increase() {
        value++;
    }

    public boolean isEmpty() {
        return value <= 0;
    }

}