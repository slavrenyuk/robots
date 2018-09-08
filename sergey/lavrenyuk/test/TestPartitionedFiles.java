package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.data.PartitionedFiles;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

public class TestPartitionedFiles extends TestSupport {

    public static void main(String[] args) {
        run(new TestPartitionedFiles());
    }

    public void testFileSupplierWithoutPlaceholder() {
        assertExceptionThrown(() -> new PartitionedFiles.FileSupplier("abc"),
                IllegalArgumentException.class, "file pattern must contain {} placeholder");
    }

    public void testFileIteratorrWithoutPlaceholder() {
        assertExceptionThrown(() -> new PartitionedFiles.FileIterator("abc"),
                IllegalArgumentException.class, "file pattern must contain {} placeholder");
    }

    public void testFileSupplier() {
        Supplier<File> supplier = new PartitionedFiles.FileSupplier("test/abc{}.dat");
        assertCondition(supplier.get().getAbsolutePath().contains("test/abc0.dat"));
        assertCondition(supplier.get().getAbsolutePath().contains("test/abc1.dat"));
    }

    public void testFileIterator() {
        Iterator<File> iterator = new PartitionedFiles.FileIterator("test/empty{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("test/empty0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("test/empty1.dat"));
        assertCondition(!iterator.hasNext());
    }

}
