package sergey.lavrenyuk.io;

import robocode.RobocodeFileOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;

public class PartitionedFileOutputStream extends OutputStream {

    private Supplier<File> files;
    private RobocodeFileOutputStream currentFileOutputStream;

    private final int maxBytesPerFile;
    private int currentFileWrittenBytes;

    public PartitionedFileOutputStream(String filePattern, int maxBytesPerFile) throws IOException {
        this(new PatternFiles.Supplier(filePattern), maxBytesPerFile);
    }

    public PartitionedFileOutputStream(Supplier<File> files, int maxBytesPerFile) throws IOException {
        this.files = files;
        this.currentFileOutputStream = new RobocodeFileOutputStream(files.get());
        this.maxBytesPerFile = maxBytesPerFile;
        this.currentFileWrittenBytes = 0;
    }

    @Override
    public void write(int b) throws IOException {
        if (currentFileWrittenBytes >= maxBytesPerFile) {
            currentFileOutputStream.flush();
            currentFileOutputStream.close();
            currentFileOutputStream = new RobocodeFileOutputStream(files.get());
            currentFileWrittenBytes = 0;
        }
        currentFileOutputStream.write(b);
        currentFileWrittenBytes++;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        int bytesLeft = maxBytesPerFile - currentFileWrittenBytes;
        if (len <= bytesLeft) {
            currentFileOutputStream.write(b, off, len);
            currentFileWrittenBytes += len;
        } else {
            currentFileOutputStream.write(b, off, bytesLeft);
            currentFileOutputStream.flush();
            currentFileOutputStream.close();
            currentFileOutputStream = new RobocodeFileOutputStream(files.get());
            currentFileWrittenBytes = 0;
            write(b, off + bytesLeft, len - bytesLeft);
        }
    }

    @Override
    public void flush() throws IOException {
        currentFileOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        currentFileOutputStream.close();
        currentFileWrittenBytes = 0; // write attempts will certainly go to the closed file
    }
}
