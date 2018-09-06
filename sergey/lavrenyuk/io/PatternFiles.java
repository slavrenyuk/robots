package sergey.lavrenyuk.io;

import java.io.File;

public class PatternFiles {

    public static final String PLACEHOLDER = "{}";

    public static class Supplier implements java.util.function.Supplier<File> {

        private final String filePattern;
        private int index;

        public Supplier(String filePattern) {
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

    public static class Iterator implements java.util.Iterator<File> {

        private final Supplier supplier;
        private File nextFile;

        public Iterator(String filePattern) {
            supplier = new Supplier(filePattern);
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
