package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.WeightMatrix;

import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;

public class TestWeightMatrix {

    public static void main(String[] args) {
        Runner.runTests(TestWeightMatrix.class);
    }

    public void testInvalidArgumentsSize() {
        assertExceptionThrown(
                () -> new WeightMatrix(
                        new float[WeightMatrix.HIDDEN_NEURONS][WeightMatrix.INPUT_NEURONS + 1],
                        new float[WeightMatrix.OUTPUT_NEURONS][WeightMatrix.HIDDEN_NEURONS + 4]),
                IllegalArgumentException.class, "15 hidden to output neuron weights expected, but found 18");
    }
}
