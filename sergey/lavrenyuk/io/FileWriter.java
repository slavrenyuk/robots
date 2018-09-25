package sergey.lavrenyuk.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

public class FileWriter<T> implements Writer<T> {

    private final OutputStream out;
    private final Function<T, byte[]> serialization;

    public FileWriter(File file, Function<T, byte[]> serialization) throws IOException {
        this(file, serialization, false);
    }

    public FileWriter(File file, Function<T, byte[]> serialization, boolean append) throws IOException {
        this.out = new BufferedOutputStream(new FileOutputStream(file, append));
        this.serialization = serialization;
    }

    @Override
    public void write(T data) throws IOException {
        out.write(serialization.apply(data));
    }

    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }
}
