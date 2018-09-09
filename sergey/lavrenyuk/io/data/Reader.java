package sergey.lavrenyuk.io.data;

import java.io.Closeable;
import java.io.IOException;

public interface Reader<T> extends Closeable {

    T read() throws IOException;
}
