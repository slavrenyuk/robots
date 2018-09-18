package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.nn.score.WeightMatrixScorerRawDataIO;

import java.io.IOException;
import java.util.Arrays;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.assertFileContents;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFile;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFiles;

public class TestWeightMatrixScorerRawDataIO {

    private final int INPUT_ITEM_SIZE = 3;
    private final int OUTPUT_ITEM_SIZE = 2;

    public static void main(String[] args) {
        Runner.runTests(TestWeightMatrixScorerRawDataIO.class);
    }

    public void testHappyPath() throws IOException {
        WeightMatrixScorerRawDataIO rawDataIO = setupAndCreateRawDataIO();

        byte[] data = rawDataIO.read();
        assertCondition(Arrays.equals(data, new byte[] {0, 1, 2}));
        rawDataIO.write(process(data));

        data = rawDataIO.read();
        assertCondition(Arrays.equals(data, new byte[] {3, 4, 5}));
        rawDataIO.write(process(data));

        data = rawDataIO.read();
        assertCondition(Arrays.equals(data, new byte[] {6, 7, 8}));
        rawDataIO.write(process(data));

        data = rawDataIO.read();
        assertCondition(Arrays.equals(data, new byte[] {9, 10, 11}));
        rawDataIO.write(process(data));

        data = rawDataIO.read();
        assertCondition(data.length == 0);
        rawDataIO.close();

        assertCondition(!IO.getFile("input_test_file_part0.dat").exists());
        assertCondition(!IO.getFile("input_test_file_part1.dat").exists());

        assertFileContents("output_test_file_part0.dat", new byte[] {1, -1, 4, -4});
        assertFileContents("output_test_file_part1.dat", new byte[] {7, -7, 10, -10});
    }

    public void testNoInputFiles() {
        assertExceptionThrown(
                () -> {
                    try {
                        new WeightMatrixScorerRawDataIO("input_test_file_part{}.dat", "output_test_file_part{}.dat",
                                INPUT_ITEM_SIZE, OUTPUT_ITEM_SIZE, 0, false);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                },
                IllegalArgumentException.class,
                "No input files found for pattern 'input_test_file_part{}.dat'");
    }

    public void testNoEnoughInputFiles() throws IOException {
        createTestFile("input_test_file_part0.dat", new byte[] {0, 1, 2, 3, 4, 5});
        createTestFile("input_test_file_part1.dat", new byte[] {6, 7, 8, 9, 10, 11});
        final int startFileIndex = 3;
        assertExceptionThrown(
                () -> {
                    try {
                        new WeightMatrixScorerRawDataIO("input_test_file_part{}.dat", "output_test_file_part{}.dat",
                                INPUT_ITEM_SIZE, OUTPUT_ITEM_SIZE, startFileIndex, false);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                },
                IllegalArgumentException.class,
                "No input files found for pattern 'input_test_file_part{}.dat'");
    }

    public void testUnexpectedRead() throws IOException {
        WeightMatrixScorerRawDataIO rawDataIO = setupAndCreateRawDataIO();
        rawDataIO.read();
        assertExceptionThrown(
                () -> {
                    try {
                        rawDataIO.read();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                },
                IllegalStateException.class,
                "Only sequential read / write methods calls are allowed");
    }

    public void testUnexpectedWrite() throws IOException {
        WeightMatrixScorerRawDataIO rawDataIO = setupAndCreateRawDataIO();
        byte[] data = rawDataIO.read();
        rawDataIO.write(process(data));
        assertExceptionThrown(
                () -> {
                    try {
                        rawDataIO.write(process(data));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                },
                IllegalStateException.class,
                "Only sequential read / write methods calls are allowed");
    }

    private byte[] process(byte[] data) {
        assertCondition(data.length == INPUT_ITEM_SIZE);
        byte sum = 0;
        for (byte b : data) {
            sum += b;
        }
        return new byte[] {(byte) (sum / INPUT_ITEM_SIZE), (byte) (-sum / INPUT_ITEM_SIZE)};
    }

    private WeightMatrixScorerRawDataIO setupAndCreateRawDataIO() throws IOException {
        createTestFile("input_test_file_part0.dat", new byte[] {0, 1, 2, 3, 4, 5});
        createTestFile("input_test_file_part1.dat", new byte[] {6, 7, 8, 9, 10, 11});
        createTestFiles("output_test_file_part0.dat", "output_test_file_part1.dat");
        return new WeightMatrixScorerRawDataIO("input_test_file_part{}.dat", "output_test_file_part{}.dat",
                INPUT_ITEM_SIZE, OUTPUT_ITEM_SIZE, 0, false);

    }
}
