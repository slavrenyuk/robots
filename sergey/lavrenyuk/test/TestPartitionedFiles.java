package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.PartitionedFiles;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFiles;

public class TestPartitionedFiles {

    public static void main(String[] args) {
        Runner.runTests(TestPartitionedFiles.class);
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
        Supplier<File> supplier = new PartitionedFiles.FileSupplier("abc{}.dat");
        assertCondition(supplier.get().getAbsolutePath().contains("abc0.dat"));
        assertCondition(supplier.get().getAbsolutePath().contains("abc1.dat"));
    }

    public void testFileIterator() throws IOException {
        createTestFiles("empty0.dat", "empty1.dat");
        Iterator<File> iterator = new PartitionedFiles.FileIterator("empty{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("empty0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("empty1.dat"));
        assertCondition(!iterator.hasNext());
    }
}
