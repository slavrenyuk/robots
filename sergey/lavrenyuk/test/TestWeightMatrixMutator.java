package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.training.utils.WeightMatrixMutator;

import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithMutationAndDelta;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestWeightMatrixMutator {

    public static void main(String[] args) {
        Runner.runTests(TestWeightMatrixMutator.class);
    }

    public void test() {
        int mutationPercentage = 10;
        WeightMatrixMutator mutator = new WeightMatrixMutator(mutationPercentage);
        WeightMatrix weightMatrix = randomMatrix();
        WeightMatrix mutatedWeightMatrix = mutator.mutate(weightMatrix);
        assertEqualsWithMutationAndDelta(weightMatrix, mutatedWeightMatrix, mutationPercentage);
    }
}
