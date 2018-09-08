package sergey.lavrenyuk.test;

import sergey.lavrenyuk.MyRobot;
import sergey.lavrenyuk.io.IO;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

public class TestSupport {

    public static final Random RANDOM = new Random();

    public static final Class<?> ROBOT_CLASS = MyRobot.class;

    public static final String DATA_DIRECTORY = ROBOT_CLASS.getName().replace(".", "/") + ".data/";

    public static void run(Object testInstance) {

        IO.initialize(
                () -> System.out,
                fileName -> {
                    String dataDirectoryFilePath = DATA_DIRECTORY + fileName;
                    // absent fileUrl means there is no such file, but we want to return a File instance anyway
                    Optional<URL> fileUrl = Optional.ofNullable(testInstance.getClass().getResource(dataDirectoryFilePath));
                    return fileUrl.map(url -> new File(url.getPath())).orElse(new File(dataDirectoryFilePath));
                }
        );

        for (Method method : testInstance.getClass().getMethods()) {
            if (method.getName().startsWith("test")) {
                try {
                    System.out.println("\nRunning " + method.getName());
                    method.invoke(testInstance);
                    System.out.println("Success!");
                } catch (ReflectiveOperationException ex) {
                    System.out.println("Failed:");
                    Optional.ofNullable(ex.getCause()).orElse(ex).printStackTrace(System.out);
                }
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

    public static float[][] randomWeights(int m, int n, int multiplier) {
        float[][] result = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = (RANDOM.nextBoolean() ? 1 : -1) * RANDOM.nextFloat() * multiplier;
            }
        }
        return result;
    }
}
