package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.NeuralNetwork;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.util.Arrays;

import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithDelta;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestNeuralNetwork {

    public static void main(String[] args) {
        Runner.runTests(TestNeuralNetwork.class);
    }

    public void testProcessNotFails() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(randomMatrix());
        float[] input = new float[WeightMatrix.INPUT_NEURONS];
        Arrays.fill(input, 0.5f);

        neuralNetwork.process(input);
    }

    public void testProcessWithSmallMatrix() {
        int inputNeurons = 2;
        int hiddenNeurons = 2;
        int outputNeurons = 2;
        // +1 for shift neuron
        float[][] inputToHiddenWeights = new float[hiddenNeurons][inputNeurons + 1];
        inputToHiddenWeights[0][0] = -0.5f;
        inputToHiddenWeights[0][1] = 0.0f;
        inputToHiddenWeights[0][2] = 0.5f;
        inputToHiddenWeights[1][0] = 1.5f;
        inputToHiddenWeights[1][1] = 2.0f;
        inputToHiddenWeights[1][2] = -1.0f;

        float[][] hiddenToOutputWeights = new float[outputNeurons][hiddenNeurons + 1];
        hiddenToOutputWeights[0][0] = -1.2f;
        hiddenToOutputWeights[0][1] = -0.7f;
        hiddenToOutputWeights[0][2] = -0.3f;
        hiddenToOutputWeights[1][0] = 1.1f;
        hiddenToOutputWeights[1][1] = -2.0f;
        hiddenToOutputWeights[1][2] = -3.0f;

        NeuralNetwork neuralNetwork = new NeuralNetwork(inputNeurons, hiddenNeurons, outputNeurons,
                inputToHiddenWeights, hiddenToOutputWeights);
        float[] result = neuralNetwork.process(new float[] {0.2f, -0.4f});
        // hidden neurons:
        // 0: -0.5 * 0.2 + 0 + 0.5 * 1 = 0.4
        // 1: 1.5 * 0.2 + 2 * (-0.4) - 1 * 1 = -1.5 -> normalized to -1.0
        // output neurons:
        // 0: -1.2 * 0.4 + -0.7 * (-1) - 0.3 * 1 = -0.08
        // 1: 1.1 * 0.4 + -2 * (-1) - 3 * 1 = -0.56
        assertEqualsWithDelta(result[0], -0.08f);
        assertEqualsWithDelta(result[1], -0.56f);
    }
}
