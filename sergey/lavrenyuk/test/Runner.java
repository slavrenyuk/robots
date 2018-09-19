package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.test.base.TestMetric;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class Runner {

    public static void main(String[] args) {
        runAllTests();
    }

    public static void runAllTests() {
        TestMetric totalMetric = new TestMetric();

        String testPackage = Runner.class.getPackage().getName();
        File testFolder = new File(Runner.class.getResource(".").getPath());

        // file name that:
        // - starts with "Test"
        // - has any number of any characters except $ (inner class marker) in the middle
        // - ends with ".class"
        Pattern pattern = Pattern.compile("Test[^\\$]*\\.class");

        Arrays.stream(testFolder.list())
                .filter(fileName -> pattern.matcher(fileName).matches())
                .forEach(fileName -> {
                    try {
                        String testClassName = testPackage + "." + fileName.substring(0, fileName.length() - ".class".length());
                        Class<?> testClass = Class.forName(testClassName);

                        System.out.println(testClass.getName());
                        TestMetric currentMetric = runTests(testClass);
                        totalMetric.add(currentMetric);

                        System.out.println("====================================================================================");

                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        System.out.println("Total " + totalMetric);
    }

    public static TestMetric runTests(Class<?> testClass) {

        File classTestFolder = initializeIOAndCreateTestFolder(testClass);

        Object testInstance;
        try {
            testInstance = testClass.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        TestMetric metric = new TestMetric();

        for (Method method : testClass.getMethods()) {
            if (method.getName().startsWith("test")) {
                try {
                    System.out.println("\nRunning " + method.getName());

                    method.invoke(testInstance);

                    metric.incrementSuccessful();
                    System.out.println("Success!");
                } catch (ReflectiveOperationException ex) {
                    metric.incrementFailed();
                    System.out.println("Failed:");
                    Optional.ofNullable(ex.getCause()).orElse(ex).printStackTrace(System.out);
                } finally {
                    Arrays.stream(classTestFolder.listFiles())
                            .forEach(File::delete);
                }
            }
        }
        classTestFolder.delete();
        System.out.println(metric);
        return metric;
    }

    private static File initializeIOAndCreateTestFolder(Class<?> testClass) {
        File classTestFolder = new File(testClass.getResource(".").getPath() + testClass.getSimpleName() + ".data");
        classTestFolder.mkdir();

        String classTestFolderName = classTestFolder.getAbsolutePath() + "/";
        IO.initialize(System.out, classTestFolder, fileName -> new File(classTestFolderName + fileName));

        return classTestFolder;
    }
}
