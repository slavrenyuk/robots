package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.scoring.WeightMatrixScorer;

import java.util.concurrent.atomic.AtomicInteger;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithDelta;
import static sergey.lavrenyuk.test.base.TestUtils.randomMatrix;

public class TestWeightMatrixScorer {

    public static void main(String[] args) {
        Runner.runTests(TestWeightMatrixScorer.class);
    }

    public void testHappyPath() {

        WeightMatrix wm1 = randomMatrix();
        WeightMatrix wm2 = randomMatrix();

        AtomicInteger reads = new AtomicInteger(0);
        AtomicInteger writes = new AtomicInteger(0);
        AtomicInteger closed = new AtomicInteger(0);

        Reader<byte[]> reader = new Reader<byte[]>() {

            @Override
            public byte[] read() {
                if (reads.incrementAndGet() == 1) {
                    return Serializer.serializeWeightMatrix(wm1);
                } else if (reads.get() == 2) {
                    return Serializer.serializeWeightMatrix(wm2);
                } else {
                    return new byte[0];
                }
            }

            @Override
            public void close() {
                closed.incrementAndGet();
            }
        };

        Writer<byte[]> writer = new Writer<byte[]>() {

            @Override
            public void write(byte[] data) {
                ScoredWeightMatrix swm = Serializer.deserializeScoredWeightMatrix(data);
                if (writes.incrementAndGet() == 1) {
                    assertCondition(swm.getWeightMatrix().equals(wm1));
                    assertEqualsWithDelta(swm.getScore().getWinRate(), 2f / 3 );
                    assertEqualsWithDelta(swm.getScore().getAverageEnergyDiff(), 20f);
                } else if (writes.get() == 2) {
                    assertCondition(swm.getWeightMatrix().equals(wm2));
                    assertEqualsWithDelta(swm.getScore().getWinRate(), 1f / 3);
                    assertEqualsWithDelta(swm.getScore().getAverageEnergyDiff(), -30f);
                } else {
                    throw new AssertionError("Only 2 writes expected");
                }
            }

            @Override
            public void close() {
                closed.incrementAndGet();
            }
        };

        final int roundPerMatrix = 3;
        WeightMatrixScorer scorer = new WeightMatrixScorer(reader, writer, roundPerMatrix);

        WeightMatrix returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm1));
        scorer.accept(new Score.RoundResult(true, 80f));
        returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm1));
        scorer.accept(new Score.RoundResult(true, 20f));
        returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm1));
        scorer.accept(new Score.RoundResult(false, -40f));

        returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm2));
        scorer.accept(new Score.RoundResult(false, -50f));
        returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm2));
        scorer.accept(new Score.RoundResult(true, 10f));
        returnedWm = scorer.get();
        assertCondition(returnedWm.equals(wm2));
        scorer.accept(new Score.RoundResult(false, -50f));

        assertCondition(scorer.get() == null);

        scorer.close();

        // scorer.get() returns the same value 3 times (test value for roundPerMatrix)
        // first 3 get() yields 1 read, next 3 get() also yields 1 read, plus the last get() yields an additional read
        assertCondition(reads.get() == 3);
        // 6 accept() results in 2 writes since roundPerMatrix = 3
        assertCondition(writes.get() == 2);
        // closed reader and writer
        assertCondition(closed.get() == 2);
    }
}
