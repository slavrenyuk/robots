package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestSerializer {

    public static void main(String[] args) {
        Runner.runTests(TestSerializer.class);
    }

    public void testWeightMatrixSerialization() {
        WeightMatrix wm = randomMatrix();
        WeightMatrix wm2 = Serializer.deserializeWeightMatrix(Serializer.serializeWeightMatrix(wm));
        assertCondition(wm.equals(wm2));
    }

    public void testScoredWeightMatrixSerialization() {
        Score score = Score.builder()
                .addRoundResult(true, 20.0f)
                .addRoundResult(true, 35.0f)
                .addRoundResult(false, -50.0f)
                .build();
        WeightMatrix weightMatrix = randomMatrix();

        ScoredWeightMatrix swm = new ScoredWeightMatrix(score, weightMatrix);
        ScoredWeightMatrix swm2 = Serializer.deserializeScoredWeightMatrix(Serializer.serializeScoredWeightMatrix(swm));
        assertCondition(swm.equals(swm2));
    }
}
