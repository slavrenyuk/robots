package sergey.lavrenyuk.nn;

import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.nn.scoring.RoundResultConsumer;
import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.WeightMatrixScorer;

import java.util.function.Supplier;

public class NeuralNetworkMode {

    public static final String RANDOM = "random";
    public static final String SCORING = "scoring";
    public static final String FIGHTING = "fighting";

    private final Supplier<WeightMatrix> weightMatrixSupplier;
    private final RoundResultConsumer roundResultConsumer;

    public NeuralNetworkMode(String mode) {
        if (RANDOM.equals(mode)) {
            Supplier<Integer> maxAbsWeightSupplier =
                    new IntGeneratorFromString(Config.getNeuralNetworkMatrixMaxAbsWeight());
            RandomWeightMatrixGenerator generator = new RandomWeightMatrixGenerator();
            this.weightMatrixSupplier = () -> generator.next(maxAbsWeightSupplier.get());
            this.roundResultConsumer = new NoOpResultConsumer();
        } else if (SCORING.equals(mode)) {
            WeightMatrixScorer weightMatrixScorer = WeightMatrixScorer.create(
                    Config.getNeuralNetworkWeightMatrixFilePattern(),
                    Config.getNeuralNetworkScoredWeightMatrixFilePattern(),
                    Config.getScoringRoundsPerMatrix(),
                    true);
            this.weightMatrixSupplier = weightMatrixScorer;
            this.roundResultConsumer = weightMatrixScorer;
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
}
