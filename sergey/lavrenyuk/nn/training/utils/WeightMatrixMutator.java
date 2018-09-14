package sergey.lavrenyuk.nn.training.utils;

import sergey.lavrenyuk.nn.WeightMatrix;

import java.util.Random;

public class WeightMatrixMutator {

    private static final Random random = new Random();

    private final float ratio;

    public WeightMatrixMutator(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage must be greater or equal to 0 and less or equal to 100");
        }
        this.ratio = ((float) percentage) / 100;
    }

    public WeightMatrix mutate(WeightMatrix weightMatrix) {
        return new WeightMatrix(
                mutate(weightMatrix.getInputToHiddenWeights()),
                mutate(weightMatrix.getHiddenToOutputWeights())
        );
    }

    private float[][] mutate(float[][] weights) {
        float[][] result = new float[weights.length][];
        for (int i = 0; i < weights.length; i++) {
            result[i] = mutate(weights[i]);
        }
        return result;
    }

    private float[] mutate(float[] weights) {
        float[] result = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            result[i] = mutate(weights[i]);
        }
        return result;
    }

    private float mutate(float weight) {
        float mutation = (2 * random.nextFloat() - 1) * ratio;
        return weight * (1 + mutation);
    }
}
