package sergey.lavrenyuk.io;

import java.io.Closeable;
import java.io.IOException;

public interface Reader<T> extends Closeable {

    // null if eof reached
    // an item otherwise
    T read() throws IOException;

    // false if eof reached
    // true otherwise
    default boolean skip() throws IOException {
        return (read() != null);
    }

    // false if eof reached
    // true otherwise
    default boolean skip(int n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("number of items to skip must be grater or equal to zero");
        }
        for (int i = 0; i < n; i++) {
            if(!skip()) {
                return false;
            }
        }
        return true;
    }
}
