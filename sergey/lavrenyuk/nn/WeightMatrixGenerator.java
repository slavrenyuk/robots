package sergey.lavrenyuk.nn;

import java.util.Random;
import java.util.function.Supplier;

public class WeightMatrixGenerator {

    private final Random RANDOM = new Random();

    public WeightMatrix generateFixed(float weight) {
        return create(() -> weight);
    }

    public WeightMatrix generateRandom() {
        return generateRandom(1);
    }

    public WeightMatrix generateRandom(float maxAbsWeight) {
        return create(() -> (RANDOM.nextBoolean() ? 1 : -1) * RANDOM.nextFloat() * maxAbsWeight);
    }

    private WeightMatrix create(Supplier<Float> weightSupplier) {
        return new WeightMatrix(
                // +1 for the shift neuron
                create(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, weightSupplier),
                create(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, weightSupplier));
    }

    private float[][] create(int m, int n, Supplier<Float> weightSupplier) {
        float[][] result = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = weightSupplier.get();
            }
        }
        return result;
    }
}
