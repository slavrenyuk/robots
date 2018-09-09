package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;

public class TestWeightMatrix extends Test {

    private final RandomWeightMatrixGenerator generator = new RandomWeightMatrixGenerator();

    public static void main(String[] args) {
        new TestWeightMatrix().run();
    }

    public void testValidArguments() {
        generator.next(100);
    }

    public void testInvalidArgumentsSize() {
        assertExceptionThrown(
                () -> new WeightMatrix(
                        new float[WeightMatrix.HIDDEN_NEURONS][WeightMatrix.INPUT_NEURONS + 1],
                        new float[WeightMatrix.OUTPUT_NEURONS][WeightMatrix.HIDDEN_NEURONS + 4]),
                IllegalArgumentException.class, "15 hidden to output neuron weights expected, but found 18");
    }
}
