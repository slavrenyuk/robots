package sergey.lavrenyuk.io.data;

import sergey.lavrenyuk.io.IO;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

public class PartitionedFiles {

    public static final String PLACEHOLDER = "{}";

    public static class FileSupplier implements Supplier<File> {

        private final String filePattern;
        private int index;

        public FileSupplier(String filePattern) {
            if (!filePattern.contains(PLACEHOLDER)) {
                throw new IllegalArgumentException(String.format("file pattern must contain %s placeholder", PLACEHOLDER));
            }
            this.filePattern = filePattern;
            this.index = 0;
        }

        @Override
        public File get() {
            return IO.getFile(filePattern.replace(PLACEHOLDER, Integer.toString(index++)));
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
}
