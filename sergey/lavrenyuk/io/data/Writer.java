package sergey.lavrenyuk.io.data;

import java.io.Closeable;
import java.io.IOException;

public interface Writer<T> extends Closeable {

    void write(T data) throws IOException;
}
