package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

import static sergey.lavrenyuk.io.Serializer.deserializeScoreFromScoredWeightMatrix;
import static sergey.lavrenyuk.io.Serializer.deserializeScoredWeightMatrix;
import static sergey.lavrenyuk.io.Serializer.deserializeWeightMatrixFromScoredWeightMatrix;
import static sergey.lavrenyuk.io.Serializer.serializeScoredWeightMatrix;
import static sergey.lavrenyuk.io.Serializer.serializeWeightMatrix;
import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

import static sergey.lavrenyuk.io.Serializer.deserializeWeightMatrix;

public class TestSerializer {

    public static void main(String[] args) {
        Runner.runTests(TestSerializer.class);
    }

    public void testWeightMatrixSerialization() {
        WeightMatrix wm = randomMatrix();
        WeightMatrix wm2 = deserializeWeightMatrix(serializeWeightMatrix(wm));
        assertCondition(wm.equals(wm2));
    }

    public void testScoredWeightMatrixSerialization() {
        Score score = createTestScore();
        WeightMatrix weightMatrix = randomMatrix();

        ScoredWeightMatrix swm = new ScoredWeightMatrix(score, weightMatrix);
        ScoredWeightMatrix swm2 = deserializeScoredWeightMatrix(serializeScoredWeightMatrix(swm));
        assertCondition(swm.equals(swm2));
    }

    public void testScoredFromScoredWeightMatrixSerialization() {
        Score score = createTestScore();
        WeightMatrix weightMatrix = randomMatrix();
        ScoredWeightMatrix scoredWeightMatrix = new ScoredWeightMatrix(score, weightMatrix);
        assertCondition(score.equals(
                deserializeScoreFromScoredWeightMatrix(serializeScoredWeightMatrix(scoredWeightMatrix))));
    }

    public void testWeightMatrixFromScoredWeightMatrixSerialization() {
        Score score = createTestScore();
        WeightMatrix weightMatrix = randomMatrix();
        ScoredWeightMatrix scoredWeightMatrix = new ScoredWeightMatrix(score, weightMatrix);
        assertCondition(weightMatrix.equals(
                deserializeWeightMatrixFromScoredWeightMatrix(serializeScoredWeightMatrix(scoredWeightMatrix))));
    }

    private static Score createTestScore() {
        return Score.builder()
                .addRoundResult(true, 20.0f)
                .addRoundResult(true, 35.0f)
                .addRoundResult(false, -50.0f)
                .build();
    }
}
