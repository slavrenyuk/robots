package sergey.lavrenyuk.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Simple writer interface
 *
 * @param <T> type of items to write
 */
public interface Writer<T> extends Closeable {

    void write(T data) throws IOException;
}
