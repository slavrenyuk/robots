package sergey.lavrenyuk.nn.scoring;

import robocode.RobocodeFileOutputStream;
import sergey.lavrenyuk.io.PartitionedFiles;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;

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

// TODO document this class doesn't support multiple threads in parallel, but it supports different thread sequential access,
// which may happen in the Robocode environment, observed when round ended because of too many skipped turns, which, in turn, presumably is caused by garbage collection
// in that case battle thread performs some manipulation over the robot code instead of the robot's thread
public class WeightMatrixScorerRawDataIO implements Reader<byte[]>, Writer<byte[]> {

    private final boolean robocodeEnvironment;
    private final int inputItemSize;
    private final int outputItemSize;

    private final Iterator<File> inputFilesIterator;
    private final Supplier<File> outputFilesSupplier;

    private volatile boolean closed = false;
    private volatile int reads = 0;
    private volatile int writes = 0;

    private volatile InputStream currentInputFileStream;
    private volatile OutputStream currentOutputFileStream;

    private volatile boolean openNextOutputFileOnWrite;
    private volatile File previousInputFile;
    private volatile File currentInputFile;

    public WeightMatrixScorerRawDataIO(String inputFilePattern,
                                       String outputFilePattern,
                                       int inputItemSize,
                                       int outputItemSize,
                                       boolean robocodeEnvironment) throws IOException {

        this.robocodeEnvironment = robocodeEnvironment;
        this.inputItemSize = inputItemSize;
        this.outputItemSize = outputItemSize;

        Iterable<Integer> fileIndexes = PartitionedFiles.getFileIndexes(inputFilePattern);
        this.inputFilesIterator = PartitionedFiles.asIterator(inputFilePattern, fileIndexes.iterator());
        this.outputFilesSupplier = PartitionedFiles.asSupplier(outputFilePattern, fileIndexes.iterator());
        if (!this.inputFilesIterator.hasNext()) {
            throw new IllegalArgumentException(String.format("No input files found for pattern '%s'", inputFilePattern));
        }

        this.currentInputFileStream = nextInputFile();
        this.currentOutputFileStream = nextOutputFile();
    }

    @Override
    public synchronized byte[] read() throws IOException {

        if (reads != writes) {
            throw new IllegalStateException(String.format("Read attempt failed - only sequential read / write methods calls " +
                    "are allowed. Reads = %d, Writes = %d", reads, writes));
        }
        reads++;

        byte[] result = new byte[inputItemSize];
        int bytesRead = currentInputFileStream.read(result);

        if (bytesRead == -1) {
            reads--;
            if (inputFilesIterator.hasNext()) {
                currentInputFileStream = nextInputFile();
                openNextOutputFileOnWrite = true;
                return read();
            }
            return new byte[0];
        }

        if (bytesRead != inputItemSize) {
            throw new IllegalArgumentException(String.format("File %s doesn't contain an integer number of input items",
                    currentInputFile.getAbsolutePath()));
        }

        return result;
    }

    @Override
    public synchronized void write(byte[] data) throws IOException {

        if (reads - 1 != writes) {
            throw new IllegalStateException(String.format("Write attempt failed - only sequential read / write methods calls " +
                    "are allowed. Reads = %d, Writes = %d", reads, writes));
        }
        writes++;

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
    public synchronized void close() throws IOException {
        if (!closed) {
            currentOutputFileStream.flush();
            currentOutputFileStream.close();

            boolean endOfInputFileReached = (currentInputFileStream.read() == -1);
            boolean allItemsWritten = (reads == writes);

            currentInputFileStream.close();
            if (endOfInputFileReached && allItemsWritten) {
                currentInputFile.delete();
            }

            closed = true;
        }
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
