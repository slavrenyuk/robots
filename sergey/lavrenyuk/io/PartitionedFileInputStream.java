package sergey.lavrenyuk.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

public class PartitionedFileInputStream extends InputStream {

    private Iterator<String> fileNames;

    private FileInputStream currentFileInputStream;

    public PartitionedFileInputStream(Iterator<String> fileNames) throws IOException {
        if (!fileNames.hasNext()) {
            throw new IllegalArgumentException("file names iterator has no elements");
        }
        this.fileNames = fileNames;
        this.currentFileInputStream = new FileInputStream(IO.getFile(fileNames.next()));
    }

    @Override
    public int read() throws IOException {
        int bytesRead = currentFileInputStream.read();
        if (bytesRead == -1 && fileNames.hasNext()) {
            currentFileInputStream.close();
            currentFileInputStream = new FileInputStream(IO.getFile(fileNames.next()));
            return read();

        }
        return bytesRead;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int bytesRead = currentFileInputStream.read(b, off, len);
        if (bytesRead < len && fileNames.hasNext()) {
            if (bytesRead == -1) {
                bytesRead = 0;
            }
            currentFileInputStream.close();
            currentFileInputStream = new FileInputStream(IO.getFile(fileNames.next()));
            return read(b, off + bytesRead, len - bytesRead) + bytesRead;
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        currentFileInputStream.close();
        fileNames = Collections.emptyIterator();
    }
}
