package sergey.lavrenyuk.io;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

// TODO add javadoc regarding the Robocode specific - getDataFile() creates a new file if not found,
// there will be a lot of files and checking directory for file presence is not efficient,
// thus there are some limitations in the implementation
public class PartitionedFiles {

    private static final String PLACEHOLDER = "{}";

    public static class FileNameSupplier implements Supplier<String> {

        private final String filePattern;
        private int index;

        public FileNameSupplier(String filePattern) {
            this(filePattern, 0);
        }

        public FileNameSupplier(String filePattern, int firstIndex) {
            if (!filePattern.contains(PLACEHOLDER)) {
                throw new IllegalArgumentException(String.format("file pattern must contain %s placeholder", PLACEHOLDER));
            }
            this.filePattern = filePattern;
            this.index = firstIndex;
        }

        @Override
        public String get() {
            return filePattern.replace(PLACEHOLDER, Integer.toString(index++));
        }
    }

    public static class FileSupplier implements Supplier<File> {

        private final FileNameSupplier fileNameSupplier;

        public FileSupplier(String filePattern) {
            this.fileNameSupplier = new FileNameSupplier(filePattern);
        }

        public FileSupplier(String filePattern, int firstIndex) {
            this.fileNameSupplier = new FileNameSupplier(filePattern, firstIndex);
        }

        @Override
        public File get() {
            return IO.getFile(fileNameSupplier.get());
        }
    }

    public static class FileIterator implements Iterator<File> {

        private final FileSupplier supplier;
        private File nextFile;

        public FileIterator(String filePattern) {
            supplier = new FileSupplier(filePattern);
            nextFile = supplier.get();
        }

        public FileIterator(String filePattern, int firstIndex) {
            supplier = new FileSupplier(filePattern, firstIndex);
            nextFile = supplier.get();
        }

        @Override
        public boolean hasNext() {
            return nextFile.exists();
        }

        @Override
        public File next() {
            File result = nextFile;
            nextFile = supplier.get();
            return result;
        }
    }

    public static String resolvePlaceholder(String filePattern, int index) {
        return filePattern.replace(PLACEHOLDER, Integer.toString(index));
    }

    public static String findNextAvailableFileName(String filePattern) {
        FileNameSupplier fileNameSupplier = new FileNameSupplier(filePattern);
        String fileName = fileNameSupplier.get();
        while (IO.getFile(fileName).exists()) {
            fileName = fileNameSupplier.get();
        }
        return fileName;
    }
}
