package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.data.PartitionedFileReader;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.test.base.BaseTest;

import java.io.IOException;
import java.nio.ByteBuffer;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.createTestFile;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestPartitionedFileReader extends BaseTest {

    public static void main(String[] args) {
        new TestPartitionedFileReader().runTests();
    }

    public void testHappyPath() throws IOException {
        WeightMatrix wm11 = randomMatrix();
        WeightMatrix wm12 = randomMatrix();
        WeightMatrix wm21 = randomMatrix();

        ByteBuffer buffer = ByteBuffer.wrap(new byte[2 * WeightMatrix.SIZE_IN_BYTES]);
        buffer.put(Serializer.serializeWeightMatrix(wm11));
        buffer.put(Serializer.serializeWeightMatrix(wm12));
        createTestFile("abc0.dat", buffer.array());
        createTestFile("abc1.dat", Serializer.serializeWeightMatrix(wm21));

        PartitionedFileReader<WeightMatrix> reader =
                new PartitionedFileReader<>("abc{}.dat", WeightMatrix.SIZE_IN_BYTES, Serializer::deserializeWeightMatrix);

        assertCondition(wm11.equals(reader.read()));
        assertCondition(wm12.equals(reader.read()));
        assertCondition(wm21.equals(reader.read()));
        assertCondition(reader.read() == null);
    }
}
