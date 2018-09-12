package sergey.lavrenyuk.io.data;

import robocode.RobocodeFileOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class PartitionedFileWriter<T> implements Writer<T> {

    private final boolean robocodeEnvironment;
    private final Supplier<File> fileSupplier;
    private final Function<T, byte[]> serialization;
    private final int itemsPerFile;

    private OutputStream out;
    private int itemsWritten;

    public PartitionedFileWriter(String filePattern, int itemsPerFile, Function<T, byte[]> serialization,
                                 boolean robocodeEnvironment) throws IOException {
        this.fileSupplier = new PartitionedFiles.FileSupplier(filePattern);
        this.itemsPerFile = itemsPerFile;
        this.serialization = serialization;
        this.robocodeEnvironment = robocodeEnvironment;
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
        OutputStream outputStream = robocodeEnvironment
                ? new RobocodeFileOutputStream(fileSupplier.get())
                : new FileOutputStream(fileSupplier.get());
        return new BufferedOutputStream(outputStream);
    }
}
