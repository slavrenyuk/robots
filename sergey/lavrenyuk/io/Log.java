package sergey.lavrenyuk.io;

import java.io.PrintStream;

public class Log {

    private static final PrintStream OUT = IO.getConsole();

    private final String className;

    public Log(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    public void error(String message) {
        OUT.println(String.format("%s: %s - %s", "ERROR", className, message));
    }

    public void warn(String message) {
        OUT.println(String.format("%s: %s - %s", "WARN", className, message));
    }

    public void info(String message) {
        OUT.println(String.format("%s: %s - %s", "INFO", className, message));
    }

    public void debug(String message) {
        OUT.println(String.format("%s: %s - %s", "DEBUG", className, message));
    }

    public void print(String message, Object... args) {
        OUT.print(String.format(message, args));
    }

    public void println(String message, Object... args) {
        OUT.println(String.format(message, args));
    }
}
