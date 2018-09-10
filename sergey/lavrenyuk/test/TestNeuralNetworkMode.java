package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.data.WeightMatrixScorer;
import sergey.lavrenyuk.nn.NeuralNetworkMode;
import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.Score;
import sergey.lavrenyuk.nn.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TestNeuralNetworkMode extends Test {

    public static void main(String[] args) {
        new TestNeuralNetworkMode().run();
    }

    public void testIncorectMode() {
        assertExceptionThrown(
                () -> new NeuralNetworkMode("INCORRECT"),
                IllegalArgumentException.class,
                "Unsupported neural network mode INCORRECT");
    }

    public void testIntGeneratorFromEmptyString() {
        assertExceptionThrown(
                () -> NeuralNetworkMode.createIntGeneratorFromString(""),
                NumberFormatException.class,
                "For input string: \"\"");
    }

    public void testIntGeneratorFromIncorrectString() {
        assertExceptionThrown(
                () -> NeuralNetworkMode.createIntGeneratorFromString("1, abc, 2"),
                NumberFormatException.class,
                "For input string: \"abc\"");
    }

    public void testIntGeneratorFromString() {
        Supplier<Integer> generator = NeuralNetworkMode.createIntGeneratorFromString("1, 1, 2");
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(2));
        assertCondition(generator.get().equals(1));
    }

    public void testTrainingSupplierAndConsumer() {

        RandomWeightMatrixGenerator random = new RandomWeightMatrixGenerator();
        WeightMatrix wm1 = random.next();
        WeightMatrix wm2 = random.next();

        AtomicInteger mockReads = new AtomicInteger(0);
        AtomicInteger mockWrites = new AtomicInteger(0);
        AtomicInteger mockClosed = new AtomicInteger(0);
        WeightMatrixScorer weightMatrixScorerMock = new WeightMatrixScorer() {

            private WeightMatrix current;

            @Override
            public boolean isWriteExpected() {
                return mockReads.get() % 3 == 0;
            }

            @Override
            public WeightMatrix getCurrentMatrix() {
                return current;
            }

            @Override
            public WeightMatrix read() throws IOException {
                current = mockReads.getAndIncrement() < 3 ? wm1 : wm2;
                return current;
            }

            @Override
            public void write(ScoredWeightMatrix data) throws IOException {
                mockWrites.incrementAndGet();

                if (!isWriteExpected()) {
                    throw new AssertionError(String.format("Unexpected write. Reads = %d", mockReads.get()));
                }
                if (data.getWeightMatrix().equals(wm1)) {
                    assertEqualsWithDelta(data.getScore().getWinRate(), 2f / 3 );
                    assertEqualsWithDelta(data.getScore().getAverageEnergyDiff(), 20f);
                } else {
                    assertEqualsWithDelta(data.getScore().getWinRate(), 1f / 3);
                    assertEqualsWithDelta(data.getScore().getAverageEnergyDiff(), -30f);
                }
            }

            @Override
            public void close() throws IOException {
                mockClosed.incrementAndGet();
            }
        };

        NeuralNetworkMode.TrainingSupplierAndConsumer t =
                new NeuralNetworkMode.TrainingSupplierAndConsumer(weightMatrixScorerMock);

        WeightMatrix returnedWm = t.get();
        assertCondition(returnedWm == wm1);
        t.accept(new Score.RoundResult(true, 80f));
        returnedWm = t.get();
        assertCondition(returnedWm == wm1);
        t.accept(new Score.RoundResult(true, 20f));
        returnedWm = t.get();
        assertCondition(returnedWm == wm1);
        t.accept(new Score.RoundResult(false, -40f));

        returnedWm = t.get();
        assertCondition(returnedWm == wm2);
        t.accept(new Score.RoundResult(false, -50f));
        returnedWm = t.get();
        assertCondition(returnedWm == wm2);
        t.accept(new Score.RoundResult(true, 10f));
        returnedWm = t.get();
        assertCondition(returnedWm == wm2);
        t.accept(new Score.RoundResult(false, -50f));

        t.close();

        assertCondition(mockReads.get() == 6);
        assertCondition(mockWrites.get() == 2);
        assertCondition(mockClosed.get() == 1);
    }
}
