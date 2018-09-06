package sergey.lavrenyuk.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

public class PartitionedFileInputStream extends InputStream {

    private Iterator<File> files;
    private FileInputStream currentFileInputStream;

    public PartitionedFileInputStream(String filePattern) throws IOException {
        this(new PatternFiles.Iterator(filePattern));
    }

    public PartitionedFileInputStream(Iterator<File> files) throws IOException {
        if (!files.hasNext()) {
            throw new IllegalArgumentException("files iterator has no elements");
        }
        this.files = files;
        this.currentFileInputStream = new FileInputStream(files.next());
    }

    @Override
    public int read() throws IOException {
        int bytesRead = currentFileInputStream.read();
        if (bytesRead == -1 && files.hasNext()) {
            currentFileInputStream.close();
            currentFileInputStream = new FileInputStream(files.next());
            return read();

        }
        return bytesRead;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int bytesRead = currentFileInputStream.read(b, off, len);
        if (bytesRead < len && files.hasNext()) {
            if (bytesRead == -1) {
                bytesRead = 0;
            }
            currentFileInputStream.close();
            currentFileInputStream = new FileInputStream(files.next());
            return read(b, off + bytesRead, len - bytesRead) + bytesRead;
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        currentFileInputStream.close();
        files = Collections.emptyIterator();
    }
}
