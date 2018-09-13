package sergey.lavrenyuk.io;

import java.io.Closeable;
import java.io.IOException;

public interface Writer<T> extends Closeable {

    void write(T data) throws IOException;
}
