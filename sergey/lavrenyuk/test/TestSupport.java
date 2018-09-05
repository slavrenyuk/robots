package sergey.lavrenyuk.test;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Random;

public class TestSupport {

    public static final Random RANDOM = new Random();

    public static void run(Object testInstance) {
        for (Method method : testInstance.getClass().getMethods()) {
            if (method.getName().startsWith("test")) {
                try {
                    System.out.println("\nRunning " + method.getName());
                    method.invoke(testInstance);
                    System.out.println("Success!");
                } catch (ReflectiveOperationException ex) {
                    System.out.println("Failed:");
                    Optional.ofNullable(ex.getCause()).orElse(ex).printStackTrace();
                }
            }
        }
    }

    public static void assertCondition(boolean condition) {
        if (!condition) {
            throw new AssertionError("condition not met");
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
