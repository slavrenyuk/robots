package sergey.lavrenyuk.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Helper for {@link PartitionedFileReader} and {@link PartitionedFileWriter}, as well as any other code that deals with
 * partitioned files. Partitioned file is a bunch of files on a disk with the same name pattern. E.g. partitioned
 * file with name pattern "abc{}.dat" may be represented on the disk by several files "abc0.dat", "abc1.dat" and "abc2.dat".
 *
 * There are two ideas behind the partitioned files:
 * <ul>
 *     <li>each file is not too big, which improves performance</li>
 *     <li>we can process small files one by one. in case if processing fails, we can start from the file that failed</li>
 * </ul>
 */
public class PartitionedFiles {

    private PartitionedFiles() {}

    private static final String PLACEHOLDER = "{}";

    /**
     * Substitute {@link #PLACEHOLDER} in the file name pattern with the provided integer.
     */
    public static String resolvePlaceholder(String fileNamePattern, int index) {
        return fileNamePattern.replace(PLACEHOLDER, Integer.toString(index));
    }

    /**
     * Check if any file corresponding to the specified file pattern exists.
     */
    public static boolean exists(String filePattern) {
        return asStream(filePattern).findAny().isPresent();
    }

    /**
     * Returns the latest (highest) index of a file corresponding to the specified file pattern. Returns -1 if file not found.
     */
    public static int latestFileIndex(String filePattern) {

        Pattern pattern = fileNamePattern(filePattern);
        int maxFileIndex = -1;

        for (String fileName : directory(filePattern).list()) {
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                int fileIndex = Integer.parseInt(matcher.group(1));
                if (fileIndex > maxFileIndex) {
                    maxFileIndex = fileIndex;
                }
            }
        }
        return maxFileIndex;
    }

    /**
     * Returns the latest (with the highest index) file name corresponding to the specified file pattern.
     * Returns null if file not found.
     */
    public static String latestFileName(String filePattern) {
        int maxFileIndex = latestFileIndex(filePattern);
        return (maxFileIndex > -1)
                ? resolvePlaceholder(filePattern, maxFileIndex)
                : null;
    }

    /**
     * Returns the next (in regards to indexes) file name corresponding to the specified file pattern.
     */
    public static String nextFileName(String filePattern) {
        return resolvePlaceholder(filePattern, latestFileIndex(filePattern) + 1);
    }

    /**
     * Get all indexes of existing files corresponding to the specified file pattern, sorted in ascending order.
     */
    public static Iterable<Integer> getFileIndexes(String filePattern) {

        Pattern pattern = fileNamePattern(filePattern);
        List<Integer> result = new ArrayList<>();

        for (String fileName : directory(filePattern).list()) {
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                result.add(Integer.valueOf(matcher.group(1)));
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Returns a {@link Stream} of files corresponding to the specified file pattern.
     *
     * Note that the returned {@link Stream} corresponds to the files that exists on the file system.
     */
    public static Stream<File> asStream(String filePattern) {
        Pattern pattern = fileNamePattern(filePattern);
        File[] files = directory(filePattern).listFiles((dir, name) -> pattern.matcher(name).matches());
        return Arrays.stream(files);
    }

    /**
     * Returns a {@link Supplier} of files corresponding to the specified file pattern.
     *
     * Note that the returned {@link Supplier} doesn't check if the files actually exists on the file system.
     */
    public static Supplier<File> asSupplier(String filePattern) {
        return new FileSupplier(filePattern, new InfiniteIndexIterator());
    }

    /**
     * Returns a {@link Supplier} of files corresponding to the specified file pattern using the provided {@code fileIndexIterator}.
     *
     * May be used in conjunction with {@link #getFileIndexes(String)}.
     */
    public static Supplier<File> asSupplier(String filePattern, Iterator<Integer> fileIndexIterator) {
        return new FileSupplier(filePattern, fileIndexIterator);
    }

    /**
     * Returns an {@link Iterator} over files corresponding to the specified file pattern.
     *
     * Note that the returned {@link Iterator} iterates over files that exists on the file system.
     */
    public static Iterator<File> asIterator(String filePattern) {
        return new FileIterator(filePattern, getFileIndexes(filePattern).iterator());
    }

    /**
     * Returns an {@link Iterator} over files corresponding to the specified file pattern using the provided {@code fileIndexIterator}.
     *
     * May be used in conjunction with {@link #getFileIndexes(String)}.
     */
    public static Iterator<File> asIterator(String filePattern, Iterator<Integer> fileIndexIterator) {
        return new FileIterator(filePattern, fileIndexIterator);
    }

    private static class FileSupplier implements Supplier<File> {

        private final Iterator<File> fileIterator;

        FileSupplier(String filePattern, Iterator<Integer> fileIndexIterator) {
            this.fileIterator = new FileIterator(filePattern, fileIndexIterator);
        }

        @Override
        public File get() {
            return fileIterator.hasNext()
                    ? fileIterator.next()
                    : null;
        }
    }

    private static class FileIterator implements Iterator<File> {

        private final String filePattern;
        private final Iterator<Integer> fileIndexIterator;

        FileIterator(String filePattern, Iterator<Integer> fileIndexIterator) {
            if (!filePattern.contains(PLACEHOLDER)) {
                throw new IllegalArgumentException(String.format("file pattern must contain %s placeholder", PLACEHOLDER));
            }
            this.filePattern = filePattern;
            this.fileIndexIterator = fileIndexIterator;
        }

        @Override
        public boolean hasNext() {
            return fileIndexIterator.hasNext();
        }

        @Override
        public File next() {
            return IO.getFile(resolvePlaceholder(filePattern, fileIndexIterator.next()));
        }
    }

    private static Pattern fileNamePattern(String filePathPattern) {
        if (!filePathPattern.contains(PLACEHOLDER)) {
            throw new IllegalArgumentException(String.format("file pattern must contain %s placeholder", PLACEHOLDER));
        }
        return Pattern.compile(fileName(filePathPattern).replace("{}", "(\\d+)"));
    }

    private static String fileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
    }

    private static File directory(String filePath) {
        int lastSeparator = filePath.lastIndexOf(File.separatorChar);
        return lastSeparator == -1
                ? IO.getBaseDirectory()
                : IO.getFile(filePath.substring(0, lastSeparator));
    }

    private static class InfiniteIndexIterator implements Iterator<Integer> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return index++;
        }
    }
}
