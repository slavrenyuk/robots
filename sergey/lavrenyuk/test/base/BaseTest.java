package sergey.lavrenyuk.test.base;

import sergey.lavrenyuk.io.IO;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public abstract class BaseTest {

    public TestMetric runTests() {

        File testFolder = initializeIOAndCreateTestFolder();

        TestMetric metric = new TestMetric();

        for (Method method : getClass().getMethods()) {
            if (method.getName().startsWith("test")) {
                try {
                    System.out.println("\nRunning " + method.getName());

                    method.invoke(this);

                    metric.incrementSuccessful();
                    System.out.println("Success!");
                } catch (ReflectiveOperationException ex) {
                    metric.incrementFailed();
                    System.out.println("Failed:");
                    Optional.ofNullable(ex.getCause()).orElse(ex).printStackTrace(System.out);
                } finally {
                    Arrays.stream(testFolder.listFiles())
                            .forEach(File::delete);
                }
            }
        }
        testFolder.delete();
        System.out.println(metric);
        return metric;
    }

    private File initializeIOAndCreateTestFolder() {
        Class<? extends BaseTest> testClass = getClass();
        String className = testClass.getSimpleName();

        File testFolder = new File(testClass.getResource(".").getPath() + className + ".data");
        testFolder.mkdir();

        String baseDir = testFolder.getAbsolutePath() + "/";
        IO.initialize(() -> System.out, fileName -> new File(baseDir + fileName));

        return testFolder;
    }
}
