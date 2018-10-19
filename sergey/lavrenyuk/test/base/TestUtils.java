package sergey.lavrenyuk.test.base;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.nn.WeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestUtils {

    private TestUtils() {}

    private static final WeightMatrixGenerator matrixGenerator = new WeightMatrixGenerator();

    private static final float FLOAT_DELTA = 0.000001f;

    public static WeightMatrix randomMatrix() {
        return matrixGenerator.generateRandom();
    }

    public static byte[] concat(byte[]... data) {
        int size = 0;
        for (int i = 0; i < data.length; i++) {
            size += data[i].length;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[size]);
        for (int i = 0; i < data.length; i++) {
            byteBuffer.put(data[i]);
        }
        return byteBuffer.array();
    }

    public static void createTestFile(String fileName, byte[] fileData) throws IOException {
        File testFile = IO.getFile(fileName);
        FileOutputStream out = new FileOutputStream(testFile);
        out.write(fileData);
        out.flush();
        out.close();
    }

    public static void createTestFile(String fileName) throws IOException {
        IO.getFile(fileName).createNewFile();
    }

    public static void createTestFiles(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            File testFile = IO.getFile(fileName);
            testFile.createNewFile();
        }
    }

    public static void deepDelete(File folder) {
        deepDelete(folder, true);
    }

    public static void deepDelete(File folder, boolean deleteSelf) {
        for (File children : folder.listFiles()) {
            if (children.isFile()) {
                children.delete();
            } else if (children.isDirectory()) {
                deepDelete(children, true);
            }
        }
        if (deleteSelf) {
            folder.delete();
        }
    }

    public static void assertCondition(boolean condition) {
        assertCondition(condition, "condition not met");
    }

    public static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void fail(String message) {
        assertCondition(false, message);
    }

    public static void assertEqualsWithDelta(float first, float second) {
        assertEqualsWithDelta(first, second, FLOAT_DELTA);
    }

    public static void assertEqualsWithDelta(float first, float second, float delta) {
        assertCondition(Math.abs(first - second) < delta, String.format("%f != %f with delta %f", first, second, delta));
    }

    public static void assertEqualsWithMutationAndDelta(WeightMatrix original, WeightMatrix mutated, int mutationPercentage) {
        assertEqualsWithMutationAndDelta(
                original.getInputToHiddenWeights(),
                mutated.getInputToHiddenWeights(),
                mutationPercentage);
        assertEqualsWithMutationAndDelta(
                original.getHiddenToOutputWeights(),
                mutated.getHiddenToOutputWeights(),
                mutationPercentage);
    }

    public static void assertEqualsWithMutationAndDelta(float[][] original, float[][] mutated, int mutationPercentage) {
        for (int i = 0; i < original.length; i++) {
            for (int j= 0; j < original[i].length; j++) {
                assertEqualsWithDelta(
                        original[i][j],
                        mutated[i][j],
                        Math.abs(original[i][j] * (mutationPercentage / 100f + FLOAT_DELTA)));
            }
        }
    }

    public static void assertExceptionThrown(Runnable execution,
                                             Class<? extends Throwable> exceptionClass,
                                             String exceptionMessage) {
        try {
            execution.run();
            fail(String.format("%s with message '%s' was expected, but not thrown", exceptionClass.getName(), exceptionMessage));
        } catch (Exception ex) {
            assertCondition(
                    ex.getClass().equals(exceptionClass) && ex.getMessage().contains(exceptionMessage),
                    String.format("%s with message '%s' was expected, but instead got %s:'%s'",
                            exceptionClass.getName(), exceptionMessage, ex.getClass().getName(), ex.getMessage()));
        }
    }

    public static void assertFileContents(String fileName, byte[] fileData) throws IOException {
        File file = IO.getFile(fileName);
        assertCondition(file.length() == fileData.length, String.format("Unexpected file %s size. Expected %d, actual %d",
                fileName, fileData.length, file.length()));

        FileInputStream in = new FileInputStream(file);
        byte[] actualFileData = new byte[fileData.length];
        in.read(actualFileData);
        in.close();
        assertCondition(Arrays.equals(fileData, actualFileData), String.format("Incorrect %s file contents", fileName));
    }
}
