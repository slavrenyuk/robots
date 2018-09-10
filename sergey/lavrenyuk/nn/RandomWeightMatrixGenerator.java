package sergey.lavrenyuk.nn;

import java.util.Random;

public class RandomWeightMatrixGenerator {

    private final Random RANDOM = new Random();

    public WeightMatrix next() {
        return next(1);
    }

    public WeightMatrix next(int maxAbsWeight) {
        return new WeightMatrix(
                randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, maxAbsWeight),
                randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, maxAbsWeight));
    }

    private float[][] randomWeights(int m, int n, int maxAbsWeight) {
        float[][] result = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = (RANDOM.nextBoolean() ? 1 : -1) * RANDOM.nextFloat() * maxAbsWeight;
            }
        }
        return result;
    }
}
