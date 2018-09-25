package sergey.lavrenyuk.nn.training;

import sergey.lavrenyuk.Perceptron;
import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Log;
import sergey.lavrenyuk.io.PartitionedFiles;
import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.IntGeneratorFromString;
import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.scoring.Score;
import sergey.lavrenyuk.nn.scoring.ScoredWeightMatrix;
import sergey.lavrenyuk.io.FileReader;
import sergey.lavrenyuk.io.FileWriter;
import sergey.lavrenyuk.io.PartitionedFileReader;
import sergey.lavrenyuk.io.PartitionedFileWriter;
import sergey.lavrenyuk.nn.training.utils.TrainerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.function.Supplier;

import static sergey.lavrenyuk.io.PartitionedFiles.asStream;
import static sergey.lavrenyuk.io.PartitionedFiles.nextFileName;

public class Trainer {

    private static final int SCORED_WEIGHT_MATRICES_TO_SHOW = 10000;

    static { // initialize IO with our robot base directory
        String robotClassName = Perceptron.class.getSimpleName();
        File baseFolder = new File(Perceptron.class.getResource(".").getPath() + robotClassName + ".data");
        String basePath = baseFolder.getAbsolutePath() + "/";

        IO.initialize(System.out, baseFolder, fileName -> new File(basePath + fileName));
    }

    private final Log log = new Log(Trainer.class);

    private String MATRIX_MAX_ABS_WEIGHT_STRING;
    private String SCORED_GENERATION_FILE_PATTERN;
    private String NEW_GENERATION_FILE_PATTERN;
    private String SURVIVORS_FILE_PATTERN;
    private String WIN_RATIO_FILE;
    private int MATRICES_PER_OUTPUT_FILE;
    private int POPULATION;
    private int SURVIVORS;
    private int CROSSINGOVER_INDIVIDUALS;
    private int MUTATED_COPIES;
    private int MUTATION_PERCENTAGE;
    private int ROUNDS_PER_MATRIX;

    public static void main(String[] args) throws IOException {
        new Trainer().run();
    }

    public void run() throws IOException {

        Scanner scanner = new Scanner(System.in);

        while(true) { // quit when user prints Q

            // initial generation
            // current generation -> survivors
            // survivors -> next generation
            // survivors -> file for fighting TBD (put to <robot>.data/enemies/<enemy_name>.dat

            // current generation info (aka scored weight matrices)
            // next generation info (aka population info)
            // next generation verbose info (aka verbose population info)
            // all generations info (aka win percentage)
            // survivors info (aka survivors file)

            log.println("\nEnter one of the next commands:");

            log.println("%s\t\textract survivors from the scored population", Command.EXTRACT_SURVIVORS);
            log.println("%s\tcreate initial generation", Command.CREATE_INITIAL_GENERATION);
            log.println("%s\t\tcreate next generation from survivors", Command.CREATE_NEXT_GENERATION);

            log.println("%s\t\tprint next generation information", Command.NEXT_GENERATION_INFO);
            log.println("%s\tprint next generation verbose information", Command.NEXT_GENERATION_INFO_VERBOSE);
            log.println("%s\t\t\tprint survivors score", Command.SURVIVORS_SCORE);
            log.println("%s\t\tprint all generations score", Command.ALL_GENERATIONS_SCORE);
            log.println("%s\tprint current generation score (top %d)", Command.CURRENT_GENERATION_SCORE,
                    SCORED_WEIGHT_MATRICES_TO_SHOW);

            log.println("%s", Command.QUIT);
            log.print("Input: ");

            Command command = Command.fromString(scanner.nextLine());

            Config.refresh();
            readConfigProperties();

            switch (command) {
                case QUIT: {
                    log.println("Bye");
                    return;
                } case EXTRACT_SURVIVORS: {
                    if (!PartitionedFiles.exists(SCORED_GENERATION_FILE_PATTERN)) {
                        log.println("There was no input files with pattern '%s' found.", SCORED_GENERATION_FILE_PATTERN);
                        continue;
                    }

                    log.println("Survivors number = %d", SURVIVORS);
                    log.println("\nConfirm and continue? Y/N");

                    if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                        processCurrentGenerationToSurvivors();
                    }
                    break;
                } case CREATE_INITIAL_GENERATION: {
                    if (PartitionedFiles.exists(NEW_GENERATION_FILE_PATTERN)) {
                        log.println("Found generation files with pattern '%s'. Delete them manually if you still want to create " +
                                "another initial generation.", NEW_GENERATION_FILE_PATTERN);
                        continue;
                    }

                    log.println("%d random weight matrices will be created as an initial generation.", POPULATION);
                    log.println("\nConfirm and continue? Y/N");

                    if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                        createInitialGeneration();
                    }
                    break;
                } case CREATE_NEXT_GENERATION: {
                    if (!PartitionedFiles.exists(SURVIVORS_FILE_PATTERN)) {
                        log.println("Survivors file with pattern '%s' not found.", SURVIVORS_FILE_PATTERN);
                        continue;
                    }

                    if (PartitionedFiles.exists(NEW_GENERATION_FILE_PATTERN)) {
                        log.println("Found generation files with pattern '%s'. Delete them manually if you still want to " +
                                "create another generation from the survivors file.", NEW_GENERATION_FILE_PATTERN);
                        continue;
                    }

                    printPopulationInfoShort();

                    log.println("\nFound survivors file indexes: %s", PartitionedFiles.getFileIndexes(SURVIVORS_FILE_PATTERN));
                    log.print("Input index (leave blank to use the latest): ");
                    String input = scanner.nextLine();

                    int index = input.trim().isEmpty()
                            ? PartitionedFiles.latestFileIndex(SURVIVORS_FILE_PATTERN)
                            : Integer.parseInt(input);
                    String fileName = PartitionedFiles.resolvePlaceholder(SURVIVORS_FILE_PATTERN, index);
                    log.println("%s will be used", fileName);

                    log.println("\nConfirm and continue? Y/N");
                    if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                        processSurvivorsToNextGeneration(fileName);
                    }
                    break;
                } case NEXT_GENERATION_INFO: {
                    printPopulationInfoShort();
                    break;
                } case NEXT_GENERATION_INFO_VERBOSE: {
                    printPopulationInfoVerbose();
                    break;
                } case SURVIVORS_SCORE: {
                    if (!PartitionedFiles.exists(SURVIVORS_FILE_PATTERN)) {
                        log.println("Survivors file with pattern '%s' not found.", SURVIVORS_FILE_PATTERN);
                        continue;
                    }

                    log.println("\nFound survivors file indexes: %s", PartitionedFiles.getFileIndexes(SURVIVORS_FILE_PATTERN));
                    log.print("Input index (leave blank to use the latest): ");
                    String input = scanner.nextLine();

                    int index = input.trim().isEmpty()
                            ? PartitionedFiles.latestFileIndex(SURVIVORS_FILE_PATTERN)
                            : Integer.parseInt(input);
                    String fileName = PartitionedFiles.resolvePlaceholder(SURVIVORS_FILE_PATTERN, index);
                    log.println("%s will be printed\n", fileName);

                    printSurvivorsFile(fileName);
                    break;
                } case CURRENT_GENERATION_SCORE: {
                    printScoredPopulation();
                    break;
                } case ALL_GENERATIONS_SCORE: {
                    printWinRatio();
                    break;
                } default: {
                    log.println("Unknown command");
                }
            }
        }
    }

    private void readConfigProperties() {
        SCORED_GENERATION_FILE_PATTERN = Config.getNeuralNetworkScoredWeightMatrixFilePattern();
        NEW_GENERATION_FILE_PATTERN = Config.getNeuralNetworkWeightMatrixFilePattern();
        SURVIVORS_FILE_PATTERN = Config.getTrainingSurvivorsFilePattern();
        WIN_RATIO_FILE = Config.getTrainingWinRatioFile();
        MATRICES_PER_OUTPUT_FILE = Config.getTrainingMatricesPerOutputFile();
        POPULATION = Config.getTrainingPopulation();
        SURVIVORS = Config.getTrainingSurvivors();
        CROSSINGOVER_INDIVIDUALS = Config.getTrainingCrossingoverIndividuals();
        MUTATED_COPIES = Config.getTrainingMutatedCopies();
        MUTATION_PERCENTAGE = Config.getTrainingMutationPercentage();
        MATRIX_MAX_ABS_WEIGHT_STRING = Config.getNeuralNetworkMatrixMaxAbsWeight();
        ROUNDS_PER_MATRIX = Config.getScoringRoundsPerMatrix();

        // verify config parameters
        if (POPULATION < 1) {
            throw new IllegalArgumentException("population must be greater or equal to 1");
        }
        if (CROSSINGOVER_INDIVIDUALS < 0) {
            throw new IllegalArgumentException("crossingover individuals must be greater or equal to 0");
        }
        if (SURVIVORS < CROSSINGOVER_INDIVIDUALS) {
            throw new IllegalArgumentException("crossingover individuals are chosen from survivals, " +
                    "so number of crossingover individuals must be less or equal to survivors number");
        }
        if (MUTATED_COPIES < 0) {
            throw new IllegalArgumentException("mutated copies must be greater or equal to 0");
        }
        if (MUTATION_PERCENTAGE < 0 || MUTATION_PERCENTAGE > 100) {
            throw new IllegalArgumentException("mutation percentage must be greater or equal to 0 and less or equal to 100");
        }
        if (ROUNDS_PER_MATRIX < 1) {
            throw new IllegalArgumentException("at least one round per matrix is expected");
        }
    }

    private void createInitialGeneration() throws IOException {

        RandomWeightMatrixGenerator randomWeightMatrixGenerator = new RandomWeightMatrixGenerator();

        Supplier<Integer> matrixMaxAbsWeightGenerator = new IntGeneratorFromString(MATRIX_MAX_ABS_WEIGHT_STRING);

        Writer<WeightMatrix> initialGenerationWriter = new PartitionedFileWriter<>(
                NEW_GENERATION_FILE_PATTERN,
                MATRICES_PER_OUTPUT_FILE,
                Serializer::serializeWeightMatrix);

        for (int i = 0; i < POPULATION; i++) {
            initialGenerationWriter.write(randomWeightMatrixGenerator.next(matrixMaxAbsWeightGenerator.get()));
        }
        initialGenerationWriter.close();
    }

    private void processCurrentGenerationToSurvivors() throws IOException {

        Reader<ScoredWeightMatrix> currentGenerationReader = new PartitionedFileReader<>(
                SCORED_GENERATION_FILE_PATTERN,
                ScoredWeightMatrix.SIZE_IN_BYTES,
                Serializer::deserializeScoredWeightMatrix);

        File survivorsFile = IO.getFile(nextFileName(SURVIVORS_FILE_PATTERN));

        Writer<ScoredWeightMatrix> survivorsWriter = new FileWriter<>(
                survivorsFile,
                Serializer::serializeScoredWeightMatrix);

        Writer<Float> winRatioWriter = new FileWriter<>(
                IO.getFile(WIN_RATIO_FILE),
                floatValue -> ByteBuffer.wrap(new byte[Float.BYTES]).putFloat(floatValue).array(),
                true);

        Runnable currentGenerationRemover = () -> asStream(SCORED_GENERATION_FILE_PATTERN).forEach(File::delete);

        TrainerUtils.processCurrentGenerationToSurvivors(
                currentGenerationReader,
                survivorsWriter,
                winRatioWriter,
                currentGenerationRemover,
                SURVIVORS);
    }

    private void processSurvivorsToNextGeneration(String survivorsFileName) throws IOException {

        File survivorsFile = IO.getFile(survivorsFileName);

        Reader<WeightMatrix> survivorsReader = new FileReader<>(
                survivorsFile,
                ScoredWeightMatrix.SIZE_IN_BYTES,
                Serializer::deserializeWeightMatrixFromScoredWeightMatrix);

        Writer<WeightMatrix> nextGenerationWriter = new PartitionedFileWriter<>(
                NEW_GENERATION_FILE_PATTERN,
                MATRICES_PER_OUTPUT_FILE,
                Serializer::serializeWeightMatrix);

        Supplier<Integer> matrixMaxAbsWeightGenerator = new IntGeneratorFromString(MATRIX_MAX_ABS_WEIGHT_STRING);

        TrainerUtils.processSurvivorsToNextGeneration(
                survivorsReader,
                nextGenerationWriter,
                matrixMaxAbsWeightGenerator,
                CROSSINGOVER_INDIVIDUALS,
                MUTATED_COPIES,
                MUTATION_PERCENTAGE,
                POPULATION);
    }

    private void printSurvivorsFile(String survivorsFileName) throws IOException {
        File survivorsFile = IO.getFile(survivorsFileName);
        if (!survivorsFile.exists()) {
            log.println("Survivors file '%s' not found", survivorsFile.getAbsolutePath());
            return;
        }
        if (!survivorsFile.isFile()) {
            log.println("'%s' is not a file", survivorsFile.getAbsolutePath());
            return;
        }
        if (survivorsFile.length() == 0) {
            log.println("Survivors file '%s' is empty", survivorsFile.getAbsolutePath());
            return;
        }
        if (survivorsFile.length() % ScoredWeightMatrix.SIZE_IN_BYTES != 0) {
            log.println("Survivors file doesn't contain an integer number of items");
        }

        log.println("Total number of survivors: %d", survivorsFile.length() / ScoredWeightMatrix.SIZE_IN_BYTES);
        log.println(" index\t| win %%\t\t| average energy diff");

        Reader<Score> reader = new FileReader<>(
                survivorsFile, ScoredWeightMatrix.SIZE_IN_BYTES, Serializer::deserializeScoreFromScoredWeightMatrix);

        printScores(reader);
    }

    private void printScoredPopulation() throws IOException {
        if (!PartitionedFiles.exists(SCORED_GENERATION_FILE_PATTERN)) {
            log.println("There was no input files with pattern '%s' found.", SCORED_GENERATION_FILE_PATTERN);
            return;
        }

        log.println(" index\t| win %%\t\t| average energy diff");

        Reader<Score> reader = new PartitionedFileReader<>(SCORED_GENERATION_FILE_PATTERN,
                ScoredWeightMatrix.SIZE_IN_BYTES, Serializer::deserializeScoreFromScoredWeightMatrix);
        reader = TrainerUtils.sortMaxValues(reader, SCORED_WEIGHT_MATRICES_TO_SHOW, Score::compareTo);

        printScores(reader);
    }

    private void printScores(Reader<Score> reader) throws IOException {
        Supplier<Integer> indexGenerator = new ScoredWeightMatrixIndexGenerator();
        int previousIndex = -1;
        int index = indexGenerator.get();
        Score score;
        while (reader.skip(index - previousIndex - 1) && ((score = reader.read()) != null)) {
            log.println(" %d\t| %.2f%%\t| %.2f", index, score.getWinRate() * 100, score.getAverageEnergyDiff());
            previousIndex = index;
            index = indexGenerator.get();
        }
        reader.close();
    }

    private void printWinRatio() throws IOException {

        File winRatioFile = IO.getFile(WIN_RATIO_FILE);

        if (!winRatioFile.exists()) {
            log.println("Win ratio file '%s' not found", winRatioFile.getAbsolutePath());
            return;
        }
        if (!winRatioFile.isFile()) {
            log.println("'%s' is not a file", winRatioFile.getAbsolutePath());
            return;
        }
        if (winRatioFile.length() % (2 * Float.BYTES) != 0) {
            log.println("Win ratio file must contain even number of float values: population and survivors average win ratio per generation");
        }

        InputStream in = new FileInputStream(winRatioFile);

        int generation = 0;
        byte[] byteArray = new byte[2 * Float.BYTES];
        while (in.read(byteArray) != -1) {
            generation++;
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            float populationAverageWinRatio = byteBuffer.getFloat();
            float survivorsAverageWinRatio = byteBuffer.getFloat();
            log.println("Generation %d survivors win percentage = %.2f%%, population win percentage = %.2f%%",
                    generation, survivorsAverageWinRatio * 100, populationAverageWinRatio * 100);
        }
        in.close();
    }

    private void printPopulationInfoVerbose() {

        log.println("\nInput parameters:");
        log.println("S = %d\t(survivors)\nC = %d\t(crossingover individuals)\nM = %d\t(mutated copies)\nP = %d\t(population)\n",
                SURVIVORS, CROSSINGOVER_INDIVIDUALS, MUTATED_COPIES, POPULATION);
        log.println("Crossingover individuals are chosen among top rated survivors and will be paired with another survivors.");

        final int crossingoverPairs = CROSSINGOVER_INDIVIDUALS * SURVIVORS - CROSSINGOVER_INDIVIDUALS * (1 + CROSSINGOVER_INDIVIDUALS) / 2;
        log.println("Number of crossingover pairs CP = (S - 1) + (S - 2) + ... + (S - C) = C * S - C * (1 + C) / 2");
        log.println("CP = %d (crossingover pairs)\n", crossingoverPairs);

        final int descendants = SURVIVORS + crossingoverPairs * 2;
        log.println("Each crossingover pair produces 2 children.");
            log.println("Let's call survivors and their children as descendants D = S + 2 * CP");
        log.println("D = %d (descendants)\n", descendants);

        final int mutatedDescendants = descendants * (1 + MUTATED_COPIES);
        log.println("After that, descendants are cloned and mutated M = %d times", POPULATION);
        log.println("Let's call descendants and their clones as mutated descendants MD = D * (1 + M)");
        log.println("MD = %d (mutated descendants)\n", mutatedDescendants);

        if (POPULATION == mutatedDescendants) {
            log.println("Population is the same as the mutated descendants number.\nAll the mutated descendants will be used as the next generation.\n");
        } else if (POPULATION > mutatedDescendants) {
            log.println("Population (P = %d) is greater than the mutated descendants number (MD = %d).\n" +
                        "Random individuals will be added (A) to the next generation A = P - MD", POPULATION, mutatedDescendants);
            log.println("A = %d (added random individuals)\n", POPULATION - mutatedDescendants);
        } else {
            log.println("Population (P = %d) is less than the mutated descendants number (MD = %d).\n" +
                        "Some of the mutated descendants will be removed (R) from the next generation R = MD - P", POPULATION, mutatedDescendants);
            log.println("R = %d (removed individuals)\n", mutatedDescendants - POPULATION);
        }

        log.println("Population = %d, rounds per individual (matrix) = %d.",
                    POPULATION, ROUNDS_PER_MATRIX, POPULATION * ROUNDS_PER_MATRIX);
        log.println("Required rounds = %d", POPULATION * ROUNDS_PER_MATRIX);
    }

    private void printPopulationInfoShort() {

        log.println("\nInput parameters:");
        log.println("%d survivors\n%d crossingover individuals\n%d mutated copies\n%d population\n",
                SURVIVORS, CROSSINGOVER_INDIVIDUALS, MUTATED_COPIES, POPULATION);

        final int crossingoverPairs = CROSSINGOVER_INDIVIDUALS * SURVIVORS - CROSSINGOVER_INDIVIDUALS * (1 + CROSSINGOVER_INDIVIDUALS) / 2;
        log.println("%d crossingover pairs", crossingoverPairs);

        final int descendants = SURVIVORS + crossingoverPairs * 2;
        log.println("%d descendants", descendants);

        final int mutatedDescendants = descendants * (1 + MUTATED_COPIES);
        log.println("%d mutated descendants\n", mutatedDescendants);

        if (POPULATION == mutatedDescendants) {
            log.println("No added or removed individuals since target population is the same as the mutated descendants number.\n");
        } else if (POPULATION > mutatedDescendants) {
            log.println("%d added random individuals\n", POPULATION - mutatedDescendants);
        } else {
            log.println("%d removed individuals\n", mutatedDescendants - POPULATION);
        }

        log.println("%d rounds required", POPULATION * ROUNDS_PER_MATRIX);
    }

    /**
     * Class for generating scored weight matrix indexes to be shown. This class is required since there will be a lot of
     * scored weight matrices and we don't want to show them all. The rule is next:
     *  - from 1st to 10th matrix increment by 1
     *  - from 15th to 100th matrix increment by 5
     *  - from 125th to 500th matrix increment by 25
     *  - from 600th to 5000th matrix increment by 100
     *  - from 5500th till end increment by 500
     */
    private static class ScoredWeightMatrixIndexGenerator implements Supplier<Integer> {

        private int index = 0;

        @Override
        public Integer get() {
            int result = index;
            index += getIncrement(index);
            return result;
        }

        private static int getIncrement(int index) {
            return (index >= 4999) ? 500 :
                   (index >= 499) ? 100 :
                   (index >= 99) ? 25 :
                   (index >= 9) ? 5 : 1;
        }
    }

    private enum Command {

        EXTRACT_SURVIVORS,
        CREATE_INITIAL_GENERATION,
        CREATE_NEXT_GENERATION,
        NEXT_GENERATION_INFO,
        NEXT_GENERATION_INFO_VERBOSE,
        SURVIVORS_SCORE,
        ALL_GENERATIONS_SCORE,
        CURRENT_GENERATION_SCORE,
        QUIT,
        UNKNOWN;

        private final String normalizedCommand;

        Command() {
            this.normalizedCommand = normalize(name());
        }

        public static Command fromString(String command) {
            String normalizedCommand = normalize(command);
            for (Command cmd : values()) {
                if (normalizedCommand.equals(cmd.normalizedCommand)) {
                    return cmd;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return normalizedCommand;
        }

        private static String normalize(String command) {
            return command.replaceAll("[_ ]", "-").toLowerCase();
        }
    }
}
