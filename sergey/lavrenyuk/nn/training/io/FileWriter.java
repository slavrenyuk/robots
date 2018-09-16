package sergey.lavrenyuk.nn.training.io;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

public class FileWriter<T> implements Writer<T> {

    private final OutputStream out;
    private final Function<T, byte[]> serialization;

    public FileWriter(String fileName, Function<T, byte[]> serialization) throws IOException {
        this(fileName, serialization, false);
    }

    public FileWriter(String fileName, Function<T, byte[]> serialization, boolean append) throws IOException {
        File file = IO.getFile(fileName);
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
