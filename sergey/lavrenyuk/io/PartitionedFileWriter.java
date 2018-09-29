package sergey.lavrenyuk.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link Writer} that writes items to a multiple files with the same name pattern, making sure there won't be more than
 * beforehand specified amount of items written to each file. E.g. file name pattern "abc{}.dat" will be resolved to file names
 * "abc0.dat", "abc1.dat", "abc2.dat" and so on.
 *
 * @param <T> type of items to write
 */
public class PartitionedFileWriter<T> implements Writer<T> {

    private final Supplier<File> fileSupplier;
    private final Function<T, byte[]> serialization;
    private final int itemsPerFile;

    private OutputStream out;
    private int itemsWritten;

    public PartitionedFileWriter(String filePattern, int itemsPerFile, Function<T, byte[]> serialization) throws IOException {
        this.fileSupplier = PartitionedFiles.asSupplier(filePattern);
        this.itemsPerFile = itemsPerFile;
        this.serialization = serialization;
        this.out = nextOutputFileStream();
        this.itemsWritten = 0;
    }

    @Override
    public void write(T data) throws IOException {
        if (itemsWritten == itemsPerFile) {
            out = nextOutputFileStream();
            itemsWritten = 0;
        }
        out.write(serialization.apply(data));
        itemsWritten++;
    }

    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }

    private OutputStream nextOutputFileStream() throws IOException {
        if (out != null) {
            out.flush();
            out.close();
        }
        return new BufferedOutputStream(new FileOutputStream(fileSupplier.get()));
    }
}
