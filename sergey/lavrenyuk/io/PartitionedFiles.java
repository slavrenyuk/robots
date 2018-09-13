package sergey.lavrenyuk.io;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

public class PartitionedFiles {

    public static final String PLACEHOLDER = "{}";

    public static class FileNameSupplier implements Supplier<String> {

        private final String filePattern;
        private int index;

        public FileNameSupplier(String filePattern) {
            if (!filePattern.contains(PLACEHOLDER)) {
                throw new IllegalArgumentException(String.format("file pattern must contain %s placeholder", PLACEHOLDER));
            }
            this.filePattern = filePattern;
            this.index = 0;
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
