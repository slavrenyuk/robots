package sergey.lavrenyuk.nn.training.io;

import sergey.lavrenyuk.io.PartitionedFiles;
import sergey.lavrenyuk.io.Reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Function;

public class PartitionedFileReader<T> implements Reader<T> {

    private final Iterator<File> fileIterator;
    private final Function<byte[], T> deserialization;
    private final byte[] dataBuffer;

    private InputStream in;

    public PartitionedFileReader(String filePattern, int itemSize, Function<byte[], T> deserialization) throws IOException{
        this.fileIterator = new PartitionedFiles.FileIterator(filePattern);
        this.dataBuffer = new byte[itemSize];
        this.deserialization = deserialization;
        if (fileIterator.hasNext()) {
            this.in = nextInputFileStream();
        } else {
            throw new IllegalArgumentException(String.format("No files found for pattern %s", filePattern));
        }
    }

    @Override
    public T read() throws IOException {
        int bytesRead = in.read(dataBuffer);
        if (bytesRead == -1) {
            if (fileIterator.hasNext()) {
                in = nextInputFileStream();
                return read();
            }
            return null;
        }

        if (bytesRead != dataBuffer.length) {
            throw new IllegalArgumentException("File doesn't contain an integer number of input items");
        }

        return deserialization.apply(dataBuffer);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private InputStream nextInputFileStream() throws IOException {
        if (in != null) {
            in.close();
        }
        File file = fileIterator.next();
        if (file.length() % dataBuffer.length != 0) {
            throw new IllegalArgumentException(String.format("File %s doesn't contain an integer number of input items",
                    file.getAbsolutePath()));
        }
        return new BufferedInputStream(new FileInputStream(file));
    }
}
