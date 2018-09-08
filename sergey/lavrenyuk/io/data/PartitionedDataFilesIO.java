package sergey.lavrenyuk.io.data;

import robocode.RobocodeFileOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Supplier;

public class PartitionedDataFilesIO implements DataIO {

    private final boolean robocodeEnvironment;
    private final int inputItemSize;
    private final int outputItemSize;

    private int itemsRead = 0;
    private int itemsWritten = 0;

    private final Iterator<File> inputFilesIterator;
    private final Supplier<File> outputFilesSupplier;

    private InputStream currentInputFileStream;
    private OutputStream currentOutputFileStream;

    private boolean openNextOutputFileOnWrite;
    private File previousInputFile;
    private File currentInputFile;



    public PartitionedDataFilesIO(String inputFilePattern,
                                  String outputFilePattern,
                                  int inputItemSize,
                                  int outputItemSize,
                                  int startFileIndex) throws IOException {
        this(inputFilePattern, outputFilePattern, inputItemSize, outputItemSize, startFileIndex, true);
    }

    public PartitionedDataFilesIO(String inputFilePattern,
                                  String outputFilePattern,
                                  int inputItemSize,
                                  int outputItemSize,
                                  int startFileIndex,
                                  boolean robocodeEnvironment) throws IOException {

        this.robocodeEnvironment = robocodeEnvironment;

        this.inputFilesIterator = new PartitionedFiles.FileIterator(inputFilePattern);
        this.outputFilesSupplier = new PartitionedFiles.FileSupplier(outputFilePattern);

        this.inputItemSize = inputItemSize;
        this.outputItemSize = outputItemSize;

        for (int i = 0; i < startFileIndex; i++) {
            if (!this.inputFilesIterator.hasNext()) {
                throw new IllegalArgumentException(String.format("Exception while skipping %d input files, only %d files found",
                        startFileIndex, i));
            }
            this.inputFilesIterator.next();
            this.outputFilesSupplier.get();
        }

        if (!this.inputFilesIterator.hasNext()) {
            throw new IllegalArgumentException("input files iterator has no elements");
        }
        this.currentInputFileStream = nextInputFile();
        this.currentOutputFileStream = nextOutputFile();
    }

    @Override
    public byte[] read() throws IOException {

        if (itemsRead++ != itemsWritten) {
            throw new IllegalStateException("Only sequential read / write methods calls are allowed");
        }

        byte[] result = new byte[inputItemSize];
        int bytesRead = currentInputFileStream.read(result);

        if (bytesRead == -1) {
            if (inputFilesIterator.hasNext()) {
                currentInputFileStream = nextInputFile();
                openNextOutputFileOnWrite = true;
                return read();
            }
            itemsRead--;
            return new byte[0];
        }

        if (bytesRead != inputItemSize) {
            throw new IllegalArgumentException(String.format("File %s doesn't contain an integer number of input items",
                    currentInputFile.getAbsolutePath()));
        }

        return result;
    }

    @Override
    public void write(byte[] data) throws IOException {

        if (itemsRead != ++itemsWritten) {
            throw new IllegalStateException("Only sequential read / write methods calls are allowed");
        }

        if (data.length != outputItemSize) {
            throw new IllegalStateException(String.format("Expected %d bytes, but received %d", outputItemSize, data.length));
        }

        if (openNextOutputFileOnWrite) {
            openNextOutputFileOnWrite = false;
            currentOutputFileStream = nextOutputFile();
            previousInputFile.delete();
        }
        currentOutputFileStream.write(data);
    }

    @Override
    public void close() throws IOException {
        currentOutputFileStream.flush();
        currentOutputFileStream.close();
        currentInputFileStream.close();
        currentInputFile.delete();
    }

    private InputStream nextInputFile() throws IOException {
        if (currentInputFileStream != null) {
            currentInputFileStream.close();
        }

        File file = inputFilesIterator.next();
        previousInputFile = currentInputFile;
        currentInputFile = file;

        if (file.length() % inputItemSize != 0) {
            throw new IllegalArgumentException(
                    String.format("File %s doesn't contain an integer number of input items", file.getAbsolutePath()));
        }
        return new BufferedInputStream(new FileInputStream(file));
    }

    private OutputStream nextOutputFile() throws IOException {
        if (currentOutputFileStream != null) {
            currentOutputFileStream.flush();
            currentOutputFileStream.close();
        }
        File file = outputFilesSupplier.get();
        OutputStream outputStream = robocodeEnvironment
                ? new RobocodeFileOutputStream(file)
                : new FileOutputStream(file);
        return new BufferedOutputStream(outputStream);
    }
}
