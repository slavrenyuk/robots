package sergey.lavrenyuk.nn;

import java.util.Arrays;

public class NeuralNetwork {

    public static final float NEURON_MIN_VALUE = -1.0f;
    public static final float NEURON_MAX_VALUE = 1.0f;
    public static final float SHIFT_NEURON_VALUE = NEURON_MAX_VALUE;

    private final int inputNeuronsNumber;
    private final int hiddenNeuronsNumber;
    private final int outputNeuronsNumber;

    private final float[][] inputToHiddenWeights;
    private final float[][] hiddenToOutputWeights;

    public NeuralNetwork(WeightMatrix weightMatrix) {
        this(WeightMatrix.INPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS, WeightMatrix.OUTPUT_NEURONS,
                weightMatrix.getInputToHiddenWeights(), weightMatrix.getHiddenToOutputWeights());
    }

    public NeuralNetwork(int inputNeuronsNumber, int hiddenNeuronsNumber, int outputNeuronsNumber,
                         float[][] inputToHiddenWeights, float[][] hiddenToOutputWeights) {
        this.inputNeuronsNumber = inputNeuronsNumber;
        this.hiddenNeuronsNumber = hiddenNeuronsNumber;
        this.outputNeuronsNumber = outputNeuronsNumber;
        this.inputToHiddenWeights = inputToHiddenWeights;
        this.hiddenToOutputWeights = hiddenToOutputWeights;
    }

    public float[] process(float[] input) {

        if (input.length != inputNeuronsNumber) {
            throw new IllegalArgumentException(String.format(
                    "%d input neurons expected, but found %d", inputNeuronsNumber, input.length));
        }

        float[] inputNeurons = normalize(Arrays.copyOf(input, input.length + 1));
        inputNeurons[inputNeurons.length - 1] = SHIFT_NEURON_VALUE;

        float[] hiddenNeurons = new float[hiddenNeuronsNumber + 1];
        for (int i = 0; i < hiddenNeuronsNumber; i++) {
            hiddenNeurons[i] = normalize(multiply(inputNeurons, inputToHiddenWeights[i]));
        }
        hiddenNeurons[hiddenNeurons.length - 1] = SHIFT_NEURON_VALUE;

        float[] outputNeurons = new float[outputNeuronsNumber];
        for (int i = 0; i < outputNeuronsNumber; i++) {
            outputNeurons[i] = normalize(multiply(hiddenNeurons, hiddenToOutputWeights[i]));
        }

        return outputNeurons;
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

    private static float[] normalize(float[] neuronValues) {
        for (int i = 0; i < neuronValues.length; i++) {
            neuronValues[i] = normalize(neuronValues[i]);
        }
        return neuronValues;
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
}
