package sergey.lavrenyuk.nn;

import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.data.WeightMatrixScorer;

import java.io.IOException;
import java.util.function.Supplier;

public class NeuralNetworkMode {

    public static final String RANDOM = "random";
    public static final String TRAINING = "training";
    public static final String FIGHTING = "fighting";

    private final Supplier<WeightMatrix> weightMatrixSupplier;
    private final RoundResultConsumer roundResultConsumer;

    public NeuralNetworkMode(String mode) {
        if (RANDOM.equals(mode)) {
            RandomWeightMatrixGenerator generator = new RandomWeightMatrixGenerator();
            this.weightMatrixSupplier = generator::next;
            this.roundResultConsumer = new NoOpResultConsumer();
        } else if (TRAINING.equals(mode)) {
            WeightMatrixScorer weightMatrixScorer = new WeightMatrixScorer(
                    Config.getString("scorer.inputFilePattern"),
                    Config.getString("scorer.outputFilePattern"),
                    Config.getInteger("scorer.startFileIndex", 0),
                    Config.getInteger("scorer.roundsPerMatrix"),
                    true);
            TrainingSupplierAndConsumer trainingSupplierAndConsumer = new TrainingSupplierAndConsumer(weightMatrixScorer);
            this.weightMatrixSupplier = trainingSupplierAndConsumer;
            this.roundResultConsumer = trainingSupplierAndConsumer;
        } else if (FIGHTING.equals(mode)) {
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("Unsupported neural network mode " + mode);
        }
    }

    public Supplier<WeightMatrix> getWeightMatrixSupplier() {
        return weightMatrixSupplier;
    }

    public RoundResultConsumer getRoundResultConsumer() {
        return roundResultConsumer;
    }

    public static class NoOpResultConsumer implements RoundResultConsumer {

        @Override
        public void accept(Score.RoundResult roundResult) { }

        @Override
        public void close() { }
    }

    public static class TrainingSupplierAndConsumer implements Supplier<WeightMatrix>, RoundResultConsumer {

        private final WeightMatrixScorer weightMatrixScorer;
        private Score.Builder scoreBuilder = Score.builder();

        public TrainingSupplierAndConsumer(WeightMatrixScorer weightMatrixScorer) {
            this.weightMatrixScorer = weightMatrixScorer;
        }

        @Override
        public WeightMatrix get() {
            try {
                return weightMatrixScorer.read();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void accept(Score.RoundResult roundResult) {
            scoreBuilder.addRoundResult(roundResult);
            if (weightMatrixScorer.isWriteExpected()) {
                try {
                    weightMatrixScorer.write(
                            new ScoredWeightMatrix(scoreBuilder.build(), weightMatrixScorer.getCurrentMatrix()));
                    scoreBuilder = Score.builder();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public void close() {
            try {
                weightMatrixScorer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
