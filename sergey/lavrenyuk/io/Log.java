package sergey.lavrenyuk.io;

import java.io.PrintStream;

public class Log {

    private static final boolean DEBUG_MODE = Config.getBoolean("log.debug", false);

    private static final PrintStream OUT = IO.getConsole();

    private final String className;

    public Log(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    public void error(Throwable t, String message) {
        error(message);
        t.printStackTrace(OUT);
    }

    public void error(String message) {
        OUT.println(String.format("%s: %s - %s", "ERROR", className, message));
    }

    public void warn(Throwable t, String message) {
        warn(message);
        t.printStackTrace(OUT);    }

    public void warn(String message) {
        OUT.println(String.format("%s: %s - %s", "WARN", className, message));
    }

    public void info(String message, Object... args) {
        OUT.println(String.format("%s: %s - %s", "INFO", className, message));
    }

    public void debug(String message, String... args) {
        if (DEBUG_MODE) {
            OUT.println(String.format("%s: %s - %s", "DEBUG", className, message));
        }
    }
}
