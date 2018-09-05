package sergey.lavrenyuk.neural.network;

import java.util.Arrays;

public class NeuralNetwork {

    public static final float NEURON_MIN_VALUE = -1.0f;
    public static final float NEURON_MAX_VALUE = 1.0f;
    public static final float SHIFT_NEURON_VALUE = NEURON_MAX_VALUE;

    private final WeightMatrix weightMatrix;

    public NeuralNetwork(WeightMatrix weightMatrix) {
        this.weightMatrix = weightMatrix;
    }

    public NeuralNetworkOutput process(NeuralNetworkInput input) {
        return transformOutput(process(transformInput(input)));
    }

    public float[] process(float[] input) {

        validateInput(input);

        float[] inputNeurons = Arrays.copyOf(input, input.length + 1);
        inputNeurons[inputNeurons.length - 1] = SHIFT_NEURON_VALUE;

        float[] hiddenNeurons = new float[WeightMatrix.HIDDEN_NEURONS + 1];
        for (int i = 0; i < WeightMatrix.HIDDEN_NEURONS; i++) {
            hiddenNeurons[i] = normalize(multiply(inputNeurons, weightMatrix.getInputToHiddenWeights()[i]));
        }
        hiddenNeurons[hiddenNeurons.length - 1] = SHIFT_NEURON_VALUE;

        float[] outputNeurons = new float[WeightMatrix.OUTPUT_NEURONS];
        for (int i = 0; i < WeightMatrix.OUTPUT_NEURONS; i++) {
            outputNeurons[i] = normalize(multiply(hiddenNeurons, weightMatrix.getHiddenToOutputWeights()[i]));
        }

        return outputNeurons;
    }

    private void validateInput(float[] input) {
        if (input.length != WeightMatrix.INPUT_NEURONS) {
            throw new IllegalArgumentException(String.format(
                    "%d input neurons expected, but found %d", WeightMatrix.INPUT_NEURONS, input.length));
        }
        for (float neuronValue : input) {
            if (neuronValue < NEURON_MIN_VALUE || neuronValue > NEURON_MAX_VALUE) {
                throw new IllegalArgumentException(String.format("Neuron value %f out of bounds [%f, %f]",
                        neuronValue, NEURON_MIN_VALUE, NEURON_MAX_VALUE));
            }
        }
    }

    private static float multiply(float[] left, float[] right) {
        if (left.length != right.length) {
            throw new IllegalArgumentException(String.format("Trying to multiply vectors of different size: %d and %d",
                    left.length, right.length));
        }

        float result = 0.0f;
        for (int i = 0; i < left.length; i++) {
            result += left[i] * right[i];
        }
        return result;
    }

    private static float normalize(float neuronValue) {
        if (neuronValue < NEURON_MIN_VALUE) {
            return NEURON_MIN_VALUE;
        }
        if (neuronValue > NEURON_MAX_VALUE) {
            return NEURON_MAX_VALUE;
        }
        return neuronValue;
    }


    private static float[] transformInput(NeuralNetworkInput input) {
        throw new UnsupportedOperationException();
    }

    private static NeuralNetworkOutput transformOutput(float[] output) {
        throw new UnsupportedOperationException();
    }
}
