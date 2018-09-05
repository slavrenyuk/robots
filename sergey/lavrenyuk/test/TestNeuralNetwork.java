package sergey.lavrenyuk.test;

import sergey.lavrenyuk.neural.network.NeuralNetwork;
import sergey.lavrenyuk.neural.network.WeightMatrix;

import java.util.Arrays;

public class TestNeuralNetwork extends TestSupport {

    public static void main(String[] args) {
        run(new TestNeuralNetwork());
    }

    public void testProcess() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(new WeightMatrix(
                randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, 100),
                randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, 100)));
        float[] input = new float[WeightMatrix.INPUT_NEURONS ];
        Arrays.fill(input, 0.5f);

        neuralNetwork.process(input);
    }
}
