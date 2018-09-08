package sergey.lavrenyuk.nn;

import java.util.Arrays;
import java.util.Objects;

public class WeightMatrix {

    public static final int INPUT_NEURONS = 10;
    public static final int HIDDEN_NEURONS = 14;
    public static final int OUTPUT_NEURONS = 4;

    // +1 because of the shift neuron
    public static final int TOTAL_WEIGHTS_NUMBER = HIDDEN_NEURONS * (INPUT_NEURONS + 1)
            + OUTPUT_NEURONS * (HIDDEN_NEURONS + 1);

    public static final int SIZE_IN_BYTES = TOTAL_WEIGHTS_NUMBER * Float.BYTES;

    private final float[][] inputToHiddenWeights;
    private final float[][] hiddenToOutputWeights;

    public WeightMatrix(float[][] inputToHiddenWeights, float[][] hiddenToOutputWeights) {
        this.inputToHiddenWeights = inputToHiddenWeights;
        this.hiddenToOutputWeights = hiddenToOutputWeights;

        for (float[] weights : inputToHiddenWeights) {
            if (weights.length != INPUT_NEURONS + 1) { // +1 because of the shift neuron
                throw new IllegalArgumentException(
                        String.format("%d input to hidden neuron weights expected, but found %d",
                                INPUT_NEURONS + 1, weights.length));
            }
        }

        if (inputToHiddenWeights.length != HIDDEN_NEURONS) {
            throw new IllegalArgumentException(String.format("%d hidden neurons expected, but found %d",
                    HIDDEN_NEURONS, inputToHiddenWeights.length));
        }

        for (float[] weights : hiddenToOutputWeights) {
            if (weights.length != HIDDEN_NEURONS + 1) {  // +1 because of the shift neuron
                throw new IllegalArgumentException(
                        String.format("%d hidden to output neuron weights expected, but found %d",
                                HIDDEN_NEURONS + 1, weights.length));
            }
        }

        if (hiddenToOutputWeights.length != OUTPUT_NEURONS) {
            throw new IllegalArgumentException(String.format("%d output neurons expected, but found %d",
                    OUTPUT_NEURONS, hiddenToOutputWeights.length));
        }
    }

    public float[][] getInputToHiddenWeights() {
        return inputToHiddenWeights;
    }

    public float[][] getHiddenToOutputWeights() {
        return hiddenToOutputWeights;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        WeightMatrix that = (WeightMatrix) obj;
        return Arrays.deepEquals(this.inputToHiddenWeights, that.inputToHiddenWeights)
                && Arrays.deepEquals(this.hiddenToOutputWeights, that.hiddenToOutputWeights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputToHiddenWeights, hiddenToOutputWeights);
    }
}
