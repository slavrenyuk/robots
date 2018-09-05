package sergey.lavrenyuk.io;

import java.io.File;
import java.io.PrintStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class IO {

    private static Supplier<PrintStream> consoleSupplier =
            () -> { throw new IllegalStateException("Uninitialized IO"); };

    private static Function<String, File> fileAccessor =
            (fileName) -> { throw new IllegalStateException("Uninitialized IO"); };

    public static void initialize(Supplier<PrintStream> consoleSupplier, Function<String, File> fileAccessor) {
        IO.consoleSupplier = consoleSupplier;
        IO.fileAccessor = fileAccessor;
    }

    public static PrintStream getConsole() {
        return consoleSupplier.get();
    }

    public static File getFile(String fileName) {
        return fileAccessor.apply(fileName);
    }
}
