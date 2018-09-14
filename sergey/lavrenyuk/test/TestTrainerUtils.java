package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.training.utils.TrainerUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithMutationAndDelta;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.fail;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestTrainerUtils {

    public static void main(String[] args) {
        Runner.runTests(TestTrainerUtils.class);
    }

    public void testConcatLazilyNoIterables() {
        assertExceptionThrown(
                TrainerUtils::concatLazily,
                IllegalArgumentException.class,
                "No iterables found"
        );
    }

    public void testConcatLazilyWithEmptyIterable() {
        Iterator<String> concatenated = TrainerUtils.concatLazily(
                Arrays.asList("a", "b"),
                Collections.emptyList(),
                Arrays.asList("c", "d", "e"))
                .iterator();

        assertCondition(concatenated.hasNext());
        assertCondition("a".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("b".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("c".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("d".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("e".equals(concatenated.next()));

        assertCondition(!concatenated.hasNext());
    }

    public void testConcatLazilyHappyPath() {
        Iterator<String> concatenated = TrainerUtils.concatLazily(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d", "e"))
                .iterator();

        assertCondition(concatenated.hasNext());
        assertCondition("a".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("b".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("c".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("d".equals(concatenated.next()));

        assertCondition(concatenated.hasNext());
        assertCondition("e".equals(concatenated.next()));

        assertCondition(!concatenated.hasNext());
    }

    public void testMutateLazilyWithEmptySourceAndNoMutations() {
        Iterator<WeightMatrix> mutated = TrainerUtils.mutateLazily(Collections.emptyList(), 0, 10).iterator();
        assertCondition(!mutated.hasNext());
    }

    public void testMutateLazilyWithEmptySource() {
        Iterator<WeightMatrix> mutated = TrainerUtils.mutateLazily(Collections.emptyList(), 2, 10).iterator();
        assertCondition(!mutated.hasNext());
    }

    public void testMutateLazilyWithNoMutations() {
        Iterator<WeightMatrix> mutated = TrainerUtils
                .mutateLazily(Arrays.asList(randomMatrix(), randomMatrix()), 0, 10)
                .iterator();
        assertCondition(!mutated.hasNext());
    }

    public void testMutateLazilyHappyPath() {
        int mutationPercentage = 25;
        Iterable<WeightMatrix> original = Arrays.asList(randomMatrix(), randomMatrix(), randomMatrix());
        Iterable<WeightMatrix> mutated = TrainerUtils.mutateLazily(original, 2, mutationPercentage);

        // to compare with 2 mutated copies
        Iterator<WeightMatrix> twoOriginalIterators = TrainerUtils.concatLazily(original, original).iterator();
        Iterator<WeightMatrix> mutatedIterator = mutated.iterator();

        while (twoOriginalIterators.hasNext() || mutatedIterator.hasNext()) {
            if (!twoOriginalIterators.hasNext() || !mutatedIterator.hasNext()) {
                fail("mutated collection must be exactly twice longer than original collection");
            }
            assertEqualsWithMutationAndDelta(twoOriginalIterators.next(), mutatedIterator.next(), mutationPercentage);
        }
    }

    public void testCrossingoverWithEmptyCollection() {
        assertCondition(TrainerUtils.crossingover(Collections.emptyList(), 0).isEmpty());
    }

    public void testCrossingoverWithException() {
        assertExceptionThrown(
                () -> TrainerUtils.crossingover(Arrays.asList(randomMatrix(), randomMatrix()), 3),
                IllegalArgumentException.class,
                "crossingover individuals are chosen from survivals, so number of crossingover individuals " +
                        "must be less or equal to survivors number");
    }

    public void testCrossingoverHappyPath() {
        WeightMatrix wm1 = randomMatrix();
        WeightMatrix wm2 = randomMatrix();
        WeightMatrix wm3 = randomMatrix();
        List<WeightMatrix> crossingover = TrainerUtils.crossingover(Arrays.asList(wm1 , wm2, wm3), 2);

        assertCondition(crossingover.size() == 6);

        assertCondition(Arrays.equals(crossingover.get(0).getInputToHiddenWeights(), wm1.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(0).getHiddenToOutputWeights(), wm2.getHiddenToOutputWeights()));

        assertCondition(Arrays.equals(crossingover.get(1).getInputToHiddenWeights(), wm2.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(1).getHiddenToOutputWeights(), wm1.getHiddenToOutputWeights()));

        assertCondition(Arrays.equals(crossingover.get(2).getInputToHiddenWeights(), wm1.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(2).getHiddenToOutputWeights(), wm3.getHiddenToOutputWeights()));

        assertCondition(Arrays.equals(crossingover.get(3).getInputToHiddenWeights(), wm3.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(3).getHiddenToOutputWeights(), wm1.getHiddenToOutputWeights()));

        assertCondition(Arrays.equals(crossingover.get(4).getInputToHiddenWeights(), wm2.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(4).getHiddenToOutputWeights(), wm3.getHiddenToOutputWeights()));

        assertCondition(Arrays.equals(crossingover.get(5).getInputToHiddenWeights(), wm3.getInputToHiddenWeights()));
        assertCondition(Arrays.equals(crossingover.get(5).getHiddenToOutputWeights(), wm2.getHiddenToOutputWeights()));
    }
}
