package sergey.lavrenyuk.test;

import sergey.lavrenyuk.neural.network.WeightMatrix;

public class TestWeightMatrix extends TestSupport {

    public static void main(String[] args) {
        run(new TestWeightMatrix());
    }

    public void testValidArguments() {
        new WeightMatrix(
                randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, 100),
                randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, 100));
    }

    public void testInvalidArgumentsSize() {
        assertExceptionThrown(
                () -> new WeightMatrix(
                        randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, 100),
                        randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 4, 100)),
                IllegalArgumentException.class, "15 hidden to output neuron weights expected, but found 18");
    }
}
