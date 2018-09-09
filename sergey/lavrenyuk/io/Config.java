package sergey.lavrenyuk.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public class Config {

    private Config() {}

    private static final Properties PROPERTIES = loadProperties("config.properties");

    public static String getString(String key, String defaultValue) {
        return getProperty(PROPERTIES, key, s -> s, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getProperty(PROPERTIES, key, Boolean::valueOf, defaultValue);
    }

    public static int getInteger(String key, int defaultValue) {
        return getProperty(PROPERTIES, key, Integer::valueOf, defaultValue);
    }

    public static String getString(String key) {
        return getProperty(PROPERTIES, key, s -> s);
    }

    public static boolean getBoolean(String key) {
        return getProperty(PROPERTIES, key, Boolean::valueOf);
    }

    public static int getInteger(String key) {
        return getProperty(PROPERTIES, key, Integer::valueOf);
    }


    private static Properties loadProperties(String fileName) {
        File file = IO.getFile(fileName);
        if (!file.exists()) {
            throw new IllegalStateException(String.format("Property file '%s' not found", file.getAbsolutePath()));
        }
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("IOException thrown when loading property file '%s'", file.getAbsolutePath()), ex);
        }
    }

    private static  <T> T getProperty(Properties properties, String key, Function<String, T> parseFunction, T defaultValue) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(parseFunction)
                .orElse(defaultValue);
    }


    private static  <T> T getProperty(Properties properties, String key, Function<String, T> parseFunction) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(parseFunction)
                .orElseThrow(() -> new AssertionError(String.format("Property %s not found", key)));
    }
}
