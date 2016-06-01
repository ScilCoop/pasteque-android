package fr.pasteque.client.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by svirch_n on 01/06/16
 * Last edited at 14:46.
 */
public class ReadList<T> implements Serializable, Iterable<T>{

    List<T> list;

    public ReadList(List<T> list) {
        this.list = list;
    }

    public T get(int index) {
        return this.list.get(index);
    }

    public int size() {
        return this.list.size();
    }

    @Override
    public Iterator<T> iterator() {
        return new ReadIterator<>(list.iterator());
    }

    public class ReadIterator<T> implements Iterator<T> {

        private final Iterator<T> iterator;

        public ReadIterator(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            // Disabled
        }
    }
}
