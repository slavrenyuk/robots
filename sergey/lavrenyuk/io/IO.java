package sergey.lavrenyuk.io;

import java.io.File;
import java.io.PrintStream;
import java.util.function.Function;

public class IO {

    private IO() {}

    private static PrintStream console;

    private static File baseDirectory;

    private static Function<String, File> fileAccessor;

    public static void initialize(PrintStream console, File baseDirectory, Function<String, File> fileAccessor) {
        IO.console = console;
        IO.baseDirectory = baseDirectory;
        IO.fileAccessor = fileAccessor;
    }

    public static PrintStream getConsole() {
        if (console == null) {
            throw new RuntimeException("Uninitialized IO");
        }
        return console;
    }

    public static File getBaseDirectory() {
        if (console == null) {
            throw new RuntimeException("Uninitialized IO");
        }
        return baseDirectory;
    }

    public static File getFile(String fileName) {
        if (fileAccessor == null) {
            throw new RuntimeException("Uninitialized IO");
        }
        return fileAccessor.apply(fileName);
    }
}
