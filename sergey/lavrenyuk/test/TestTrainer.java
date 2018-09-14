package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.score.Score;
import sergey.lavrenyuk.nn.score.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.training.Trainer;
import sergey.lavrenyuk.nn.training.utils.AverageEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithDelta;
import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithMutationAndDelta;
import static sergey.lavrenyuk.test.base.TestUtils.fail;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestTrainer {

    public static void main(String[] args) {
        Runner.runTests(TestTrainer.class);
    }

    public void test() throws IOException {

        // ===================      currentGenerationReader      ==========================
        Score s1 = new Score(0.6f, 20);
        Score s2 = new Score(0.8f, 60); // 2nd highest score
        Score s3 = new Score(0.7f, 30);
        Score s4 = new Score(1.0f, 80); // 1st highest score
        Score s5 = new Score(0.3f, -20);
        Score s6 = new Score(0.0f, -80);
        Score s7 = new Score(0.8f, 50); // 3rd highest score
        Score s8 = new Score(0.0f, -100);
        final float AVERAGE_POPULATION_WIN_RATIO = new AverageEvaluator()
                .put(s1.getWinRate())
                .put(s2.getWinRate())
                .put(s3.getWinRate())
                .put(s4.getWinRate())
                .put(s5.getWinRate())
                .put(s6.getWinRate())
                .put(s7.getWinRate())
                .put(s8.getWinRate())
                .getAverage();
        final float AVERAGE_SURVIVORS_WIN_RATIO = new AverageEvaluator()
                .put(s2.getWinRate())
                .put(s4.getWinRate())
                .put(s7.getWinRate())
                .getAverage();
        ScoredWeightMatrix scm1 = new ScoredWeightMatrix(s1, randomMatrix());
        ScoredWeightMatrix scm2 = new ScoredWeightMatrix(s2, randomMatrix()); // 2nd survival
        ScoredWeightMatrix scm3 = new ScoredWeightMatrix(s3, randomMatrix());
        ScoredWeightMatrix scm4 = new ScoredWeightMatrix(s4, randomMatrix()); // 1st survival
        ScoredWeightMatrix scm5 = new ScoredWeightMatrix(s5, randomMatrix());
        ScoredWeightMatrix scm6 = new ScoredWeightMatrix(s6, randomMatrix());
        ScoredWeightMatrix scm7 = new ScoredWeightMatrix(s7, randomMatrix()); // 3rd survival
        ScoredWeightMatrix scm8 = new ScoredWeightMatrix(s8, randomMatrix());
        AtomicInteger currentGenerationReaderClosed = new AtomicInteger(0);

        Reader<ScoredWeightMatrix> currentGenerationReader = new Reader<ScoredWeightMatrix>() {
            private final Iterator<ScoredWeightMatrix> currentGenerationIterator =
                    Arrays.asList(scm1, scm2, scm3, scm4, scm5, scm6, scm7, scm8).iterator();
            @Override
            public ScoredWeightMatrix read() {
                return currentGenerationIterator.hasNext() ? currentGenerationIterator.next() : null;
            }
            @Override
            public void close() {
                currentGenerationReaderClosed.incrementAndGet();
            }
        };

        // ===================      nextGenerationWriter      ==========================
        List<WeightMatrix> nextGeneration = new ArrayList<>();
        AtomicInteger nextGenerationWriterClosed = new AtomicInteger(0);

        Writer<WeightMatrix> nextGenerationWriter = new Writer<WeightMatrix>() {
            @Override
            public void write(WeightMatrix weightMatrix) {
                nextGeneration.add(weightMatrix);
            }
            @Override
            public void close() {
                nextGenerationWriterClosed.incrementAndGet();
            }
        };

        // ===================      survivorsWriter      ==========================
        List<ScoredWeightMatrix> survivorsList = new ArrayList<>();
        AtomicInteger survivorsWriterClosed = new AtomicInteger(0);
        Writer<ScoredWeightMatrix> survivorsWriter = new Writer<ScoredWeightMatrix>() {
            @Override
            public void write(ScoredWeightMatrix scoredWeightMatrix) {
                survivorsList.add(scoredWeightMatrix);
            }
            @Override
            public void close() {
                survivorsWriterClosed.incrementAndGet();
            }
        };

        // ===================      winRatioWriter      ==========================
        List<Float> winRatios = new ArrayList<>();
        AtomicInteger winRatioWriterClosed = new AtomicInteger(0);
        Writer<Float> winRatioWriter = new Writer<Float>() {
            @Override
            public void write(Float value) {
                winRatios.add(value);
            }
            @Override
            public void close() {
                winRatioWriterClosed.incrementAndGet();
            }
        };

        // ===================      currentGenerationRemover      ==========================
        AtomicInteger currentGenerationRemoved = new AtomicInteger(0);
        Runnable currentGenerationRemover =
                () -> {
                    if (currentGenerationReaderClosed.get() == 0) {
                        fail("Current generation must be removed after corresponding reader is closed.");
                    }
                    currentGenerationRemoved.incrementAndGet();
                };

        // ===================      trainer      ==========================
        final int POPULATION = 20;
        final int MUTATION_PERCENTAGE = 15;
        Trainer trainer = new Trainer(
            currentGenerationReader,
                nextGenerationWriter,
                survivorsWriter,
                winRatioWriter,
                currentGenerationRemover,
                3, // survivors
                2, // crossingoverIndividuals
                1, // mutatedCopies
                MUTATION_PERCENTAGE, // mutationPercentage
                POPULATION, // population
                () -> 1 // matrixMaxAbsWeightGenerator, not very important here
        );

        trainer.run();

        // crossingover pairs = 3
        // children = crossingover pairs * 2 = 6
        // descendants = survivors + children = 3 + 3 * 2 = 9
        // descendants * (1 + mutated copies) = 18
        // population = 20, so 2 random matrices will be added

        // verify nextGeneration
        assertCondition(currentGenerationReaderClosed.get() == 1);
        assertCondition(nextGenerationWriterClosed.get() == 1);
        assertCondition(nextGeneration.size() == POPULATION);

        assertCondition(scm4.getWeightMatrix().equals(nextGeneration.get(0)));
        assertCondition(scm2.getWeightMatrix().equals(nextGeneration.get(1)));
        assertCondition(scm7.getWeightMatrix().equals(nextGeneration.get(2)));

        WeightMatrix child42 = new WeightMatrix(
                scm4.getWeightMatrix().getInputToHiddenWeights(),
                scm2.getWeightMatrix().getHiddenToOutputWeights()
        );
        WeightMatrix child24 = new WeightMatrix(
                scm2.getWeightMatrix().getInputToHiddenWeights(),
                scm4.getWeightMatrix().getHiddenToOutputWeights()
        );
        assertCondition(child42.equals(nextGeneration.get(3)));
        assertCondition(child24.equals(nextGeneration.get(4)));

        WeightMatrix child47 = new WeightMatrix(
                scm4.getWeightMatrix().getInputToHiddenWeights(),
                scm7.getWeightMatrix().getHiddenToOutputWeights()
        );
        WeightMatrix child74 = new WeightMatrix(
                scm7.getWeightMatrix().getInputToHiddenWeights(),
                scm4.getWeightMatrix().getHiddenToOutputWeights()
        );
        assertCondition(child47.equals(nextGeneration.get(5)));
        assertCondition(child74.equals(nextGeneration.get(6)));

        WeightMatrix child27 = new WeightMatrix(
                scm2.getWeightMatrix().getInputToHiddenWeights(),
                scm7.getWeightMatrix().getHiddenToOutputWeights()
        );
        WeightMatrix child72 = new WeightMatrix(
                scm7.getWeightMatrix().getInputToHiddenWeights(),
                scm2.getWeightMatrix().getHiddenToOutputWeights()
        );
        assertCondition(child27.equals(nextGeneration.get(7)));
        assertCondition(child72.equals(nextGeneration.get(8)));

        assertEqualsWithMutationAndDelta(scm4.getWeightMatrix(), nextGeneration.get(9), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(scm2.getWeightMatrix(), nextGeneration.get(10), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(scm7.getWeightMatrix(), nextGeneration.get(11), MUTATION_PERCENTAGE);

        assertEqualsWithMutationAndDelta(child42, nextGeneration.get(12), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(child24, nextGeneration.get(13), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(child47, nextGeneration.get(14), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(child74, nextGeneration.get(15), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(child27, nextGeneration.get(16), MUTATION_PERCENTAGE);
        assertEqualsWithMutationAndDelta(child72, nextGeneration.get(17), MUTATION_PERCENTAGE);

        // verify survivorsList
        assertCondition(survivorsWriterClosed.get() == 1);
        assertCondition(survivorsList.size() == 3);
        assertCondition(scm4.equals(survivorsList.get(0)));
        assertCondition(scm2.equals(survivorsList.get(1)));
        assertCondition(scm7.equals(survivorsList.get(2)));

        // verify winRatios
        assertCondition(winRatioWriterClosed.get() == 1);
        assertCondition(winRatios.size() == 2);
        assertEqualsWithDelta(AVERAGE_POPULATION_WIN_RATIO, winRatios.get(0));
        assertEqualsWithDelta(AVERAGE_SURVIVORS_WIN_RATIO, winRatios.get(1));

        // verify currentGenerationRemover
        assertCondition(currentGenerationRemoved.get() == 1);
    }
}
