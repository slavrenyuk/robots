package sergey.lavrenyuk.nn;

import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.FileReader;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.nn.scoring.RoundResultConsumer;
import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.WeightMatrixScorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * TODO
 */
public class NeuralNetworkMode {

    public static final String RANDOM = "random";
    public static final String SCORING = "scoring";
    public static final String FIGHTING = "fighting";

    private final Supplier<WeightMatrix> weightMatrixSupplier;
    private final RoundResultConsumer roundResultConsumer;

    public NeuralNetworkMode(String mode) throws IOException {
        switch (mode) {
            case RANDOM: {
                Supplier<Integer> maxAbsWeightSupplier =
                        new IntGeneratorFromString(Config.getNeuralNetworkMatrixMaxAbsWeight());
                RandomWeightMatrixGenerator generator = new RandomWeightMatrixGenerator();

                this.weightMatrixSupplier = () -> generator.next(maxAbsWeightSupplier.get());
                this.roundResultConsumer = new NoOpResultConsumer();

                break;
            } case SCORING: {
                WeightMatrixScorer weightMatrixScorer = WeightMatrixScorer.create(
                        Config.getNeuralNetworkWeightMatrixFilePattern(),
                        Config.getNeuralNetworkScoredWeightMatrixFilePattern(),
                        Config.getScoringRoundsPerMatrix(),
                        true);

                this.weightMatrixSupplier = weightMatrixScorer;
                this.roundResultConsumer = weightMatrixScorer;

                break;
            } case FIGHTING: {
                String enemyFileName = Config.getNeuralNetworkEnemyFileName();
                File enemyFile = IO.getFile(enemyFileName);
                // Robocode automatically creates an empty file if it was not found
                if (!enemyFile.exists() || enemyFile.length() == 0) {
                    throw new IllegalArgumentException(
                            String.format("Enemy file '%s' not found", enemyFile));
                }

                Reader<WeightMatrix> weightMatrixReader =
                        new FileReader<>(enemyFile, WeightMatrix.SIZE_IN_BYTES, Serializer::deserializeWeightMatrix);
                List<WeightMatrix> weightMatrixList = readAll(weightMatrixReader);

                this.weightMatrixSupplier = new RandomElementSupplier<>(weightMatrixList);
                this.roundResultConsumer = new NoOpResultConsumer();

                break;
            } default: {
                throw new IllegalArgumentException("Unsupported neural network mode " + mode);
            }
        }
    }

    public Supplier<WeightMatrix> getWeightMatrixSupplier() {
        return weightMatrixSupplier;
    }

    public RoundResultConsumer getRoundResultConsumer() {
        return roundResultConsumer;
    }

    /**
     * Supplier that is initialized with a list of elements, each invocation of {@link #get()} returns a random element
     * of the list.
     * @param <T> type of the elements
     */
    public static class RandomElementSupplier<T> implements Supplier<T> {

        private final Random random = new Random();
        private final List<T> list;

        public RandomElementSupplier(List<T> list) {
            this.list = list;
        }

        @Override
        public T get() {
            return list.get(random.nextInt(list.size()));
        }
    }

    /**
     * Consumer that simply ignores the input data
     */
    public static class NoOpResultConsumer implements RoundResultConsumer {

        @Override
        public void accept(Score.RoundResult roundResult) { }

        @Override
        public void close() { }
    }

    public static <T> List<T> readAll(Reader<T> reader) throws IOException {
        List<T> result = new ArrayList<>();
        T obj;
        while((obj = reader.read()) != null) {
            result.add(obj);
        }
        reader.close();
        return result;
    }
}
