package sergey.lavrenyuk.nn.training.io;

import sergey.lavrenyuk.io.Reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class FileReader<T> implements Reader<T> {

    private final InputStream in;
    private final byte[] dataBuffer;
    private final Function<byte[], T> deserialization;

    public FileReader(File file, int itemSize, Function<byte[], T> deserialization) throws IOException {
        this.in = new BufferedInputStream(new FileInputStream(file));
        this.dataBuffer = new byte[itemSize];
        this.deserialization = deserialization;
    }

    @Override
    public T read() throws IOException {
        return (readBytesIntoBuffer() != -1)
                ? deserialization.apply(dataBuffer)
                : null;
    }

    @Override
    public boolean skip() throws IOException {
        return (readBytesIntoBuffer() != -1);
    }

    private int readBytesIntoBuffer() throws IOException {
        int bytesRead = in.read(dataBuffer);
        if ((bytesRead != -1) && (bytesRead != dataBuffer.length)) {
            throw new IllegalArgumentException("File doesn't contain an integer number of input items");
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
