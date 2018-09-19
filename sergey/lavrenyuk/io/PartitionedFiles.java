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

public class PartitionedFiles {

    private static final String PLACEHOLDER = "{}";

    public static boolean exists(String filePattern) {
        return asStream(filePattern).findAny().isPresent();
    }

    public static String nextFileName(String filePattern) {

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
        return resolvePlaceholder(filePattern, maxFileIndex + 1);
    }

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

    public static Stream<File> asStream(String filePattern) {
        Pattern pattern = fileNamePattern(filePattern);
        File[] files = directory(filePattern).listFiles((dir, name) -> pattern.matcher(name).matches());
        return Arrays.stream(files);
    }

    public static Supplier<File> asSupplier(String filePattern) {
        return new FileSupplier(filePattern, new InfiniteIndexIterator());
    }

    public static Supplier<File> asSupplier(String filePattern, Iterator<Integer> fileIndexIterator) {
        return new FileSupplier(filePattern, fileIndexIterator);
    }

    public static Iterator<File> asIterator(String filePattern) {
        return new FileIterator(filePattern, getFileIndexes(filePattern).iterator());
    }

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

    private static String resolvePlaceholder(String fileNamePattern, int index) {
        return fileNamePattern.replace(PLACEHOLDER, Integer.toString(index));
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
