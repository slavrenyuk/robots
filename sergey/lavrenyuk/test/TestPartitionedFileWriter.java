package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.PartitionedFileWriter;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.IOException;

import static sergey.lavrenyuk.test.base.TestUtils.assertFileContents;
import static sergey.lavrenyuk.test.base.TestUtils.concat;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFiles;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestPartitionedFileWriter {

    public static void main(String[] args) {
        Runner.runTests(TestPartitionedFileWriter.class);
    }

    public void testHappyPath() throws IOException {
        createTestFiles("abc0.dat", "abc1.dat", "abc2.dat");
        PartitionedFileWriter<WeightMatrix> writer = new PartitionedFileWriter<>("abc{}.dat", 2, Serializer::serializeWeightMatrix);

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
