package sergey.lavrenyuk.io;

import robocode.AdvancedRobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Provides values stored in {@link #CONFIG_PROPERTIES_FILE_NAME} file in the {@link AdvancedRobot#getDataDirectory() robot's folder}.
 * Relies on {@link IO}.
 *
 * @see IO
 */
public class Config {

    private Config() {}

    private static String CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    private static Properties PROPERTIES = loadProperties(CONFIG_PROPERTIES_FILE_NAME);

    public static void refresh() {
        PROPERTIES = loadProperties(CONFIG_PROPERTIES_FILE_NAME);
    }

    public static String getRobotMode() {
        return getString("robot.mode");
    }

    public static String getNeuralNetworkEnemyFileName() {
        return getString("neuralNetwork.enemyFileName");
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

    public static Integer getScoringRoundsPerMatrix() {
        return getInteger("scoring.roundsPerMatrix");
    }

    public static Integer getTrainingMatricesPerOutputFile() {
        return getInteger("training.matricesPerOutputFile");
    }

    public static String getTrainingWinRatioFile() {
        return getString("training.winRatioFile");
    }

    public static String getTrainingSurvivorsFilePattern() {
        return getString("training.survivorsFilePattern");
    }

    public static Integer getTrainingPopulation() {
        return getInteger("training.population");
    }

    public static Integer getTrainingSurvivors() {
        return getInteger("training.survivors");
    }

    public static Integer getTrainingCrossingoverIndividuals() {
        return getInteger("training.crossingoverIndividuals");
    }

    public static Integer getTrainingMutatedCopies() {
        return getInteger("training.mutatedCopies");
    }

    public static Integer getTrainingMutationPercentage() {
        return getInteger("training.mutationPercentage");
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
