package sergey.lavrenyuk;

import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.FileReader;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.nn.IntGeneratorFromString;
import sergey.lavrenyuk.nn.WeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.scoring.RoundResultConsumer;
import sergey.lavrenyuk.nn.scoring.WeightMatrixScorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

/**
 * TODO
 */
public class RobotMode {

    private static final String RANDOM = "random";
    private static final String SCORING = "scoring";
    private static final String FIGHTING = "fighting";

    private final Supplier<WeightMatrix> weightMatrixSupplier;
    private final RoundResultConsumer roundResultConsumer;

    public RobotMode(String mode) throws IOException {
        switch (mode) {
            case RANDOM: {
                Supplier<Integer> maxAbsWeightSupplier =
                        new IntGeneratorFromString(Config.getNeuralNetworkMatrixMaxAbsWeight());
                WeightMatrixGenerator generator = new WeightMatrixGenerator();

                this.weightMatrixSupplier = () -> generator.generateRandom(maxAbsWeightSupplier.get());
                this.roundResultConsumer = null;

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
                this.roundResultConsumer = null;

                break;
            } default: {
                throw new IllegalArgumentException("Unsupported robot mode " + mode);
            }
        }
    }

    public Supplier<WeightMatrix> getWeightMatrixSupplier() {
        return weightMatrixSupplier;
    }

    public Optional<RoundResultConsumer> getRoundResultConsumer() {
        return Optional.ofNullable(roundResultConsumer);
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
