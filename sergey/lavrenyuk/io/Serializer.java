package sergey.lavrenyuk.io;

import sergey.lavrenyuk.nn.score.Score;
import sergey.lavrenyuk.nn.score.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.nio.ByteBuffer;

public class Serializer {

    private Serializer() {}

    public static byte[] serializeScoredWeightMatrix(ScoredWeightMatrix scoredWeightMatrix) {
        Score score = scoredWeightMatrix.getScore();
        WeightMatrix weightMatrix = scoredWeightMatrix.getWeightMatrix();

        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[Score.SIZE_IN_BYTES + WeightMatrix.SIZE_IN_BYTES]);

        byteBuffer.putFloat(score.getWinRate());
        byteBuffer.putFloat(score.getAverageEnergyDiff());

        serializeWeightMatrix(weightMatrix, byteBuffer);

        return byteBuffer.array();
    }

    public static byte[] serializeWeightMatrix(WeightMatrix weightMatrix) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[WeightMatrix.SIZE_IN_BYTES]);
        serializeWeightMatrix(weightMatrix, byteBuffer);
        return byteBuffer.array();
    }

    private static void serializeWeightMatrix(WeightMatrix weightMatrix, ByteBuffer byteBuffer) {
        for (float[] weights : weightMatrix.getInputToHiddenWeights()) {
            for (float weight : weights) {
                byteBuffer.putFloat(weight);
            }
        }
        for (float[] weights : weightMatrix.getHiddenToOutputWeights()) {
            for (float weight : weights) {
                byteBuffer.putFloat(weight);
            }
        }
    }

    public static ScoredWeightMatrix deserializeScoredWeightMatrix(byte[] bytes) {
        verifyBytesLength(bytes.length, Score.SIZE_IN_BYTES + WeightMatrix.SIZE_IN_BYTES);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Score score = new Score(byteBuffer.getFloat(), byteBuffer.getFloat());
        WeightMatrix weightMatrix = deserializeWeightMatrix(byteBuffer);
        return new ScoredWeightMatrix(score, weightMatrix);
    }

    public static WeightMatrix deserializeWeightMatrix(byte[] bytes) {
        verifyBytesLength(bytes.length, WeightMatrix.SIZE_IN_BYTES);
        return deserializeWeightMatrix(ByteBuffer.wrap(bytes));
    }

    public static Score deserializeScoreFromScoredWeightMatrix(byte[] bytes) {
        verifyBytesLength(bytes.length, Score.SIZE_IN_BYTES + WeightMatrix.SIZE_IN_BYTES);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new Score(byteBuffer.getFloat(), byteBuffer.getFloat());
    }

    private static void verifyBytesLength(int actualSize, int expectedSize) {
        if (actualSize != expectedSize) {
            throw new IllegalArgumentException(String.format("Incorrect number of bytes. Got %d, expected %d.",
                    actualSize, expectedSize));
        }
    }

    private static WeightMatrix deserializeWeightMatrix(ByteBuffer byteBuffer) {
        // +1 because of the shift neuron
        float[][] inputToHiddenWeights = new float[WeightMatrix.HIDDEN_NEURONS][WeightMatrix.INPUT_NEURONS + 1];
        float[][] hiddenToOutputWeights = new float[WeightMatrix.OUTPUT_NEURONS][WeightMatrix.HIDDEN_NEURONS + 1];

        for (int i = 0; i < WeightMatrix.HIDDEN_NEURONS; i++) {
            for (int j = 0; j < WeightMatrix.INPUT_NEURONS + 1; j++) {
                inputToHiddenWeights[i][j] = byteBuffer.getFloat();
            }
        }

        for (int i = 0; i < WeightMatrix.OUTPUT_NEURONS; i++) {
            for (int j = 0; j < WeightMatrix.HIDDEN_NEURONS + 1; j++) {
                hiddenToOutputWeights[i][j] = byteBuffer.getFloat();
            }
        }

        return new WeightMatrix(inputToHiddenWeights, hiddenToOutputWeights);
    }
}
