package sergey.lavrenyuk.io;

import java.io.File;
import java.io.PrintStream;
import java.util.function.Function;

/**
 * Robocode has several restrictions in regarding to input and output operations:
 *  <ul>
 *      <li>Output console must be obtained by {@link robocode.Robot#out}</li>
 *      <li>
 *          Each robot can write and read files only from its directory, which is located at the same folder
 *          as the robot's main class and it is named as robot name + ".data" suffix.
 *          E.g. for {@link sergey.lavrenyuk.Perceptron} is it {@code Perceptron.data}.
 *          Within the Robocode application, files must be obtained by {@link robocode.AdvancedRobot#getDataFile(String)}.
 *          Root directory must be obtained by {@link robocode.AdvancedRobot#getDataDirectory()}
 *      </li>
 *  </ul>
 *
 * Since there is a lot of different classes in this project that deal with console and files, I had to create this class as a
 * level of abstraction. Otherwise I would have to pass {@link sergey.lavrenyuk.Perceptron} to many other classes, just to get
 * {@link robocode.Robot#out} and {@link robocode.AdvancedRobot#getDataDirectory()}.
 *
 * This class has only static methods. It must be initialized in the robot's main method, or in any other suitable place
 * like test set up.
 *
 * All code that deals with console or files, must use:
 * <ul>
 *     <li>{@link #getConsole()} instead of {@link System#out}</li>
 *     <li>
 *         {@link #getFile(String)} and {@link #getBaseDirectory()} instead of {@link File#File(String)},
 *         or any other {@link File} constructor
 *     </li>
 * </ul>
 *
 * Note: Robocode has an unusual behaviour in regards to not found files. Instead of returning a {@link File} that doesn't
 * exist (i.e. {@link File#exists()} returns false), it creates an empty file if it was not found. You should keep that in mind
 * and always check if {@link File#length()} is zero, instead of just checking {@link File#exists()}.
 */
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
