package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Test {

    // ==============================================================================================================
    // Section for running all tests from subclasses

    private static final List<Class<? extends Test>> SUBCLASSES = Arrays.asList(
            TestNeuralNetwork.class,
            TestNeuralNetworkMode.class,
            TestWeightMatrix.class,
            TestWeightMatrixScorer.class,
            TestWeightMatrixScorerRawDataIO.class,
            TestPartitionedFiles.class,
            TestSerializer.class,
            TestMaxValuesStorage.class
    );

    public static void main(String[] args) {
        for (Class<? extends Test> subclass : SUBCLASSES) {
            System.out.println("====================================================================================");
            System.out.println(subclass.getName());
            try {
                Test subclassTest = subclass.newInstance();
                subclassTest.run();

            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println();
        }
    }

    // ==============================================================================================================
    // Section for running test methods of a subclass
    //  Method is considered as test if it has "test" at the beginning of its name

    private static final RandomWeightMatrixGenerator matrixGenerator = new RandomWeightMatrixGenerator();

    private List<File> testFiles;

    public void run() {
        Class<? extends Test> testClass = getClass();
        String className = testClass.getSimpleName();

        File testDataFolder = new File(testClass.getResource(".").getPath() + className + ".data");
        boolean testDataFolderCreated = testDataFolder.mkdir();

        String baseDir = testDataFolder.getAbsolutePath() + "/";
        IO.initialize(() -> System.out, fileName -> new File(baseDir + fileName));

        for (Method method : testClass.getMethods()) {
            if (method.getName().startsWith("test")) {
                try {
                    System.out.println("\nRunning " + method.getName());

                    testFiles = new ArrayList<>();
                    method.invoke(this);

                    System.out.println("Success!");
                } catch (ReflectiveOperationException ex) {
                    System.out.println("Failed:");
                    Optional.ofNullable(ex.getCause()).orElse(ex).printStackTrace(System.out);
                } finally {
                    testFiles.forEach(File::delete);
                }
            }
        }

        if (testDataFolderCreated) {
            testDataFolder.delete();
        }
    }

    protected void withTestFile(String fileName, byte[] fileData, boolean deleteOnExit) throws IOException {
        File testFile = IO.getFile(fileName);
        FileOutputStream out = new FileOutputStream(testFile);
        out.write(fileData);
        if (deleteOnExit) {
            this.testFiles.add(testFile);
        }
    }

    // files that will be automatically created before and deleted after test execution
    protected void withTestFiles(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            File testFile = IO.getFile(fileName);
            testFile.createNewFile();
            this.testFiles.add(testFile);
        }
    }

    // ==============================================================================================================
    // Section that provides utility methods for testing

    public static void assertCondition(boolean condition) {
        if (!condition) {
            throw new AssertionError("condition not met");
        }
    }

    public static void assertEqualsWithDelta(float first, float second) {
        if (Math.abs(first - second) > 0.000001f) {
            throw new AssertionError(String.format("%f != %f", first, second));
        }
    }

    public static void assertExceptionThrown(Runnable execution,
                                             Class<? extends Throwable> exceptionClass,
                                             String exceptionMessage) {
        try {
            execution.run();
            throw new AssertionError(String.format("%s with message '%s' was expected, but not thrown",
                    exceptionClass.getName(), exceptionMessage));
        } catch (Exception ex) {
            if (!ex.getClass().equals(exceptionClass) || !ex.getMessage().contains(exceptionMessage)) {
                throw new AssertionError(String.format("%s with message '%s' was expected, but instead got %s:'%s'",
                        exceptionClass.getName(), exceptionMessage, ex.getClass().getName(), ex.getMessage()));
            }
        }
    }

    public static WeightMatrix randomMatrix() {
        return matrixGenerator.next();
    }
}
