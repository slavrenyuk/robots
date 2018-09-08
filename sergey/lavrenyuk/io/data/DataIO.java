package sergey.lavrenyuk.io.data;

import java.io.IOException;

public interface DataIO {

    byte[] read() throws IOException;

    void write(byte[] data) throws IOException;

    void close() throws IOException;
}
