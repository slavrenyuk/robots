package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.neural.network.Score;
import sergey.lavrenyuk.neural.network.ScoredWeightMatrix;
import sergey.lavrenyuk.neural.network.WeightMatrix;

public class TestSerializer extends TestSupport {

    public static void main(String[] args) {
        run(new TestSerializer());
    }

    public void testWeightMatrixSerialization() {
        WeightMatrix wm = new WeightMatrix(
                randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, 100),
                randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, 100));
        WeightMatrix wm2 = Serializer.deserializeWeightMatrix(Serializer.serializeWeightMatrix(wm));
        assertCondition(wm.equals(wm2));
    }

    public void testScoredWeightMatrixSerialization() {
        Score score = Score.builder()
                .addRoundResult(true, 20.0f)
                .addRoundResult(true, 35.0f)
                .addRoundResult(false, -50.0f)
                .build();
        WeightMatrix weightMatrix = new WeightMatrix(
                randomWeights(WeightMatrix.HIDDEN_NEURONS, WeightMatrix.INPUT_NEURONS + 1, 100),
                randomWeights(WeightMatrix.OUTPUT_NEURONS, WeightMatrix.HIDDEN_NEURONS + 1, 100));

        ScoredWeightMatrix swm = new ScoredWeightMatrix(score, weightMatrix);
        ScoredWeightMatrix swm2 = Serializer.deserializeScoredWeightMatrix(Serializer.serializeScoredWeightMatrix(swm));
        assertCondition(swm.equals(swm2));
    }
}
