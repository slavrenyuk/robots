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
        assertExceptionThrown(() -> PartitionedFiles.asSupplier("abc"),
                IllegalArgumentException.class, "file pattern must contain {} placeholder");
    }

    public void testFileSupplier() {
        Supplier<File> supplier = PartitionedFiles.asSupplier("abc{}.dat");
        assertCondition(supplier.get().getAbsolutePath().contains("abc0.dat"));
        assertCondition(supplier.get().getAbsolutePath().contains("abc1.dat"));
    }

    public void testFileIterator() throws IOException {
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        Iterator<Integer> indexIterator = PartitionedFiles.getFileIndexes("abc{}.dat").iterator();
        Iterator<File> iterator = PartitionedFiles.asIterator("abc{}.dat", indexIterator);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(!iterator.hasNext());
    }

    public void testIteratorWithGap() throws IOException {
        createTestFile("abc0.dat", "abc0".getBytes());
        createTestFile("abc1.dat", "abc1".getBytes());
        createTestFile("abc3.dat", "abc3".getBytes());
        createTestFile("abc4.dat", "abc4".getBytes());

        Iterator<Integer> indexIterator = PartitionedFiles.getFileIndexes("abc{}.dat").iterator();
        Iterator<File> iterator = PartitionedFiles.asIterator("abc{}.dat", indexIterator);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc0.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc1.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc3.dat"));
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next().getAbsolutePath().contains("abc4.dat"));
        assertCondition(!iterator.hasNext());

        assertCondition(IO.getFile("abc0.dat").exists());
        assertCondition(IO.getFile("abc1.dat").exists());
        assertCondition(!IO.getFile("abc2.dat").exists());
        assertCondition(IO.getFile("abc3.dat").exists());
        assertCondition(IO.getFile("abc4.dat").exists());
    }

    public void testGetExistingFileIndexes() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat");
        Iterator<Integer> iterator = PartitionedFiles.getFileIndexes("abc{}.dat").iterator();
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 0);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 1);
        assertCondition(!iterator.hasNext());
    }

    public void testGetExistingFileIndexesWithGaps() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat", "abc3.dat");
        Iterator<Integer> iterator = PartitionedFiles.getFileIndexes("abc{}.dat").iterator();
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 0);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 1);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 3);
        assertCondition(!iterator.hasNext());
    }

    public void testGetExistingFileIndexesInSubFolder() throws IOException {
        File folder = IO.getFile("data");
        folder.mkdir();
        createTestFiles("data/abc0.dat", "data/abc1.dat", "data/abc3.dat");
        Iterator<Integer> iterator = PartitionedFiles.getFileIndexes("data/abc{}.dat").iterator();
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 0);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 1);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 3);
        assertCondition(!iterator.hasNext());
        folder.delete();
    }

    public void testGetExistingFileIndexesInParentFolder() throws IOException {
        File folder = IO.getFile("../data");
        folder.mkdir();
        createTestFiles("../data/abc1.dat", "../data/abc3.dat", "../data/abc5.dat");
        Iterator<Integer> iterator = PartitionedFiles.getFileIndexes("../data/abc{}.dat").iterator();
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 1);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 3);
        assertCondition(iterator.hasNext());
        assertCondition(iterator.next() == 5);
        assertCondition(!iterator.hasNext());
        folder.delete();
    }

    public void testFindNextAvailableFileName() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat");
        assertCondition(("abc2.dat".equals(PartitionedFiles.nextFileName("abc{}.dat"))));
    }

    public void testFindNextAvailableFileNameWithGaps() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat", "abc3.dat");
        assertCondition(("abc4.dat".equals(PartitionedFiles.nextFileName("abc{}.dat"))));
    }

    public void testFindNextAvailableFileInSubFolder() throws IOException {
        File folder = IO.getFile("data");
        folder.mkdir();
        createTestFiles("data/abc0.dat", "data/abc1.dat", "data/abc3.dat");
        assertCondition(("data/abc4.dat".equals(PartitionedFiles.nextFileName("data/abc{}.dat"))));
        folder.delete();
    }

    public void testFindNextAvailableFileInParentFolder() throws IOException {
        File folder = IO.getFile("../data");
        folder.mkdir();
        createTestFiles("../data/abc1.dat", "../data/abc3.dat", "../data/abc5.dat");
        assertCondition(("../data/abc6.dat".equals(PartitionedFiles.nextFileName("../data/abc{}.dat"))));
        folder.delete();
    }
}
