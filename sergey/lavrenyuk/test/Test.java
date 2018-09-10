package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.IO;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Test {

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

    // files that will be automatically created before and deleted after test execution
    protected void withTempFiles(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            File testFile = IO.getFile(fileName);
            if (testFile.createNewFile()) {
                // add and later delete only those files that where created inside of this method
                this.testFiles.add(testFile);
            }
        }
    }

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
}
