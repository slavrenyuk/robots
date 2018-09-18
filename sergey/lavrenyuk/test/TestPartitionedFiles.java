package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.PartitionedFiles;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFile;
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
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        Iterator<File> iterator = new PartitionedFiles.FileIterator("abc{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(!iterator.hasNext());
    }

    public void testIteratorWithEmptyFile() throws IOException {
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        createTestFile("abc2.dat"); // empty file

        Iterator<File> iterator = new PartitionedFiles.FileIterator("abc{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(!iterator.hasNext());

        assertCondition(IO.getFile("abc0.dat").exists());
        assertCondition(IO.getFile("abc1.dat").exists());
        assertCondition(!IO.getFile("abc2.dat").exists());
    }

    public void testIteratorWithEmptyFileGap() throws IOException {
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        createTestFile("abc2.dat"); // empty file
        createTestFile("abc3.dat", "abc3".getBytes());
        createTestFile("abc4.dat", "abc4".getBytes());

        Iterator<File> iterator = new PartitionedFiles.FileIterator("abc{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(!iterator.hasNext());

        assertCondition(IO.getFile("abc0.dat").exists());
        assertCondition(IO.getFile("abc1.dat").exists());
        assertCondition(!IO.getFile("abc2.dat").exists());
        assertCondition(IO.getFile("abc3.dat").exists());
        assertCondition(IO.getFile("abc4.dat").exists());
    }

    public void testIteratorWithGap() throws IOException {
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        createTestFile("abc3.dat", "abc3".getBytes());
        createTestFile("abc4.dat", "abc4".getBytes());

        Iterator<File> iterator = new PartitionedFiles.FileIterator("abc{}.dat");
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(!iterator.hasNext());

        assertCondition(IO.getFile("abc0.dat").exists());
        assertCondition(IO.getFile("abc1.dat").exists());
        assertCondition(!IO.getFile("abc2.dat").exists());
        assertCondition(IO.getFile("abc3.dat").exists());
        assertCondition(IO.getFile("abc4.dat").exists());
    }

    public void testFindNextAvailableFileName() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat");
        assertCondition(("abc2.dat".equals(PartitionedFiles.findNextAvailableFileName("abc{}.dat"))));
    }
}
