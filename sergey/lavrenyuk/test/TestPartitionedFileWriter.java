package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.data.PartitionedFileWriter;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.test.base.BaseTest;

import java.io.IOException;

import static sergey.lavrenyuk.test.base.TestUtils.assertFileContents;
import static sergey.lavrenyuk.test.base.TestUtils.concat;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFiles;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestPartitionedFileWriter extends BaseTest {

    public static void main(String[] args) {
        new TestPartitionedFileWriter().runTests();
    }

    public void testHappyPath() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat", "abc2.dat");
        PartitionedFileWriter<WeightMatrix> writer =
                new PartitionedFileWriter<>("abc{}.dat", 2, Serializer::serializeWeightMatrix, false);

        WeightMatrix wm11 = randomMatrix();
        WeightMatrix wm12 = randomMatrix();
        WeightMatrix wm21 = randomMatrix();
        WeightMatrix wm22 = randomMatrix();
        WeightMatrix wm31 = randomMatrix();

        writer.write(wm11);
        writer.write(wm12);
        writer.write(wm21);
        writer.write(wm22);
        writer.write(wm31);
        writer.close();

        assertFileContents("abc0.dat", concat(Serializer.serializeWeightMatrix(wm11), Serializer.serializeWeightMatrix(wm12)));
        assertFileContents("abc1.dat", concat(Serializer.serializeWeightMatrix(wm21), Serializer.serializeWeightMatrix(wm22)));
        assertFileContents("abc2.dat", Serializer.serializeWeightMatrix(wm31));
    }
}
