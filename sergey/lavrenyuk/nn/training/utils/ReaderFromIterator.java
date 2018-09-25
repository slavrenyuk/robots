package sergey.lavrenyuk.nn.training.utils;

import sergey.lavrenyuk.io.Reader;

import java.util.Iterator;

public class ReaderFromIterator<T> implements Reader<T> {

    private final Iterator<T> iterator;

    public ReaderFromIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public T read() {
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void close() { }
}
