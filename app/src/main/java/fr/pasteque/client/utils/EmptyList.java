package fr.pasteque.client.utils;

import java.util.Iterator;
import java.util.List;

/**
 * Created by svirch_n on 01/06/16
 * Last edited at 14:49.
 */
public class EmptyList<T> extends ReadList<T> {

    public EmptyList() {
        super(null);
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new EmptyIterator();
    }

    private class EmptyIterator implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }
}
