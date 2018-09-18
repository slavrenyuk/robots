package sergey.lavrenyuk.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public class Config {

    private Config() {}

    private static Properties PROPERTIES = loadProperties("config.properties");

    public static void refresh() {
        PROPERTIES = loadProperties("config.properties");
    }

    public static String getNeuralNetworkMode() {
        return getString("neuralNetwork.mode");
    }

    public static String getNeuralNetworkMatrixMaxAbsWeight() {
        return getString("neuralNetwork.matrixMaxAbsWeight");
    }

    public static String getNeuralNetworkWeightMatrixFilePattern() {
        return getString("neuralNetwork.weightMatrixFilePattern");
    }

    public static String getNeuralNetworkScoredWeightMatrixFilePattern() {
        return getString("neuralNetwork.scoredWeightMatrixFilePattern");
    }

    public static Integer getScorerRoundsPerMatrix() {
        return getInteger("scorer.roundsPerMatrix");
    }

    public static Integer getScorerStartFileIndex() {
        return getInteger("scorer.startFileIndex");
    }

    public static Integer getTrainerMatricesPerOutputFile() {
        return getInteger("trainer.matricesPerOutputFile");
    }

    public static String getTrainerWinRatioFile() {
        return getString("trainer.winRatioFile");
    }

    public static String getTrainerSurvivorsFilePattern() {
        return getString("trainer.survivorsFilePattern");
    }

    public static Integer getTrainerPopulation() {
        return getInteger("trainer.population");
    }

    public static Integer getTrainerSurvivors() {
        return getInteger("trainer.survivors");
    }

    public static Integer getTrainerCrossingoverIndividuals() {
        return getInteger("trainer.crossingoverIndividuals");
    }

    public static Integer getTrainerMutatedCopies() {
        return getInteger("trainer.mutatedCopies");
    }

    public static Integer getTrainerMutationPercentage() {
        return getInteger("trainer.mutationPercentage");
    }

    private static String getString(String key) {
        return getProperty(PROPERTIES, key, s -> s);
    }

    private static int getInteger(String key) {
        return getProperty(PROPERTIES, key, Integer::valueOf);
    }

    private static  <T> T getProperty(Properties properties, String key, Function<String, T> parseFunction) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(parseFunction)
                .orElseThrow(() -> new AssertionError(String.format("Property '%s' not found", key)));
    }

    private static Properties loadProperties(String fileName) {
        File file = IO.getFile(fileName);
        if (!file.exists() || file.length() == 0) { // Robocode automatically creates an empty file if it was not found
            throw new IllegalStateException(String.format("Property file '%s' not found or empty", file.getAbsolutePath()));
        }
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("IOException thrown when loading property file '%s'", file.getAbsolutePath()), ex);
        }
    }

}
