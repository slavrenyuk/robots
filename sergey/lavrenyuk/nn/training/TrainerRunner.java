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
import sergey.lavrenyuk.nn.training.io.FileReader;
import sergey.lavrenyuk.nn.training.io.FileWriter;
import sergey.lavrenyuk.nn.training.io.PartitionedFileReader;
import sergey.lavrenyuk.nn.training.io.PartitionedFileWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.function.Supplier;

import static sergey.lavrenyuk.io.PartitionedFiles.asStream;
import static sergey.lavrenyuk.io.PartitionedFiles.nextFileName;

import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.sortMaxValues;

public class TrainerRunner {

    private static final int SCORED_WEIGHT_MATRICES_TO_SHOW = 10000;

    static { // initialize IO with our robot base directory
        String robotClassName = Perceptron.class.getSimpleName();
        File baseFolder = new File(Perceptron.class.getResource(".").getPath() + robotClassName + ".data");
        String basePath = baseFolder.getAbsolutePath() + "/";

        IO.initialize(System.out, baseFolder, fileName -> new File(basePath + fileName));
    }

    private final Log log = new Log(TrainerRunner.class);

    private String MATRIX_MAX_ABS_WEIGHT_STRING;
    private String CURRENT_GENERATION_FILE_PATTERN;
    private String NEXT_GENERATION_FILE_PATTERN;
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
        new TrainerRunner().run();
    }

    public void run() throws IOException {

        Scanner scanner = new Scanner(System.in);

        while(true) { // quit when user prints Q

            Config.refresh();
            readConfigProperties();

            // current generation -> survivors TBD
            // survivors -> next generation TBD
            // survivors -> file for fighting TBD (put to <robot>.data/enemies/<enemy_name>.dat
            // current generation -> next generation

            // current generation info (aka scored weight matrices)
            // next generation info (aka population info)
            // next generation verbose info (aka verbose population info)
            // all generations info (aka win percentage)
            // survivors info (aka survivors file)

            log.println("\nChoose one of the next options:");
            log.println("T  - training");
            log.println("P  - population info");
            log.println("Pv - verbose population info");
            log.println("S  - survivors file");
            log.println("SM - scored matrices file (top %d entries)", SCORED_WEIGHT_MATRICES_TO_SHOW);
            log.println("W  - win percentage");
            log.println("Q  - quit");
            log.print("Input: ");

            String input = scanner.nextLine();

            if ("Q".equalsIgnoreCase(input)) { // quit
                log.println("Bye");
                return;
            }

            if ("T".equalsIgnoreCase(input)) { // training

                if (PartitionedFiles.exists(CURRENT_GENERATION_FILE_PATTERN)) {
                    printPopulationInfoShort();

                    log.println("\nConfirm and continue? Y/N");

                    if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                        performTraining();
                    }
                } else {
                    log.println("There was no input files with pattern '%s' found.", CURRENT_GENERATION_FILE_PATTERN);
                    log.println("%d random weight matrices will be created as an initial population.", POPULATION);

                    log.println("\nConfirm and continue? Y/N");

                    if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                        performInitialTraining();
                    }
                }

            } else if ("P".equalsIgnoreCase(input)) { // short population info

                printPopulationInfoShort();

            } else if ("Pv".equalsIgnoreCase(input)) { // verbose population info

                printPopulationInfoVerbose();

            } else if ("S".equalsIgnoreCase(input)) { // survivors

                if (!PartitionedFiles.exists(SURVIVORS_FILE_PATTERN)) {
                    log.println("Survivors file with pattern '%s' not found", SURVIVORS_FILE_PATTERN);
                    return;
                }

                log.print("\nGeneration number (leave blank for the latest generation): ");
                input = scanner.nextLine();

                int index = input.trim().isEmpty()
                        ? PartitionedFiles.latestFileIndex(SURVIVORS_FILE_PATTERN)
                        : Integer.parseInt(input);

                printSurvivorsFile(PartitionedFiles.resolvePlaceholder(SURVIVORS_FILE_PATTERN, index));

            } else if ("SM".equalsIgnoreCase(input)) { // scored weight matrices

                printCurrentGenerationFile();

            } else if ("W".equalsIgnoreCase(input)) { // win percentage

                printWinRatioFile();

            } else {

                log.println("Invalid input");

            }
        }
    }

    private void readConfigProperties() {
        CURRENT_GENERATION_FILE_PATTERN = Config.getNeuralNetworkScoredWeightMatrixFilePattern();
        NEXT_GENERATION_FILE_PATTERN = Config.getNeuralNetworkWeightMatrixFilePattern();
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

    private void performInitialTraining() throws IOException {

        RandomWeightMatrixGenerator randomWeightMatrixGenerator = new RandomWeightMatrixGenerator();

        Supplier<Integer> matrixMaxAbsWeightGenerator = new IntGeneratorFromString(MATRIX_MAX_ABS_WEIGHT_STRING);

        Writer<WeightMatrix> initialGenerationWriter = new PartitionedFileWriter<>(
                NEXT_GENERATION_FILE_PATTERN,
                MATRICES_PER_OUTPUT_FILE,
                Serializer::serializeWeightMatrix);

        for (int i = 0; i < POPULATION; i++) {
            initialGenerationWriter.write(randomWeightMatrixGenerator.next(matrixMaxAbsWeightGenerator.get()));
        }
        initialGenerationWriter.close();
    }

    private void performTraining() throws IOException {

        Reader<ScoredWeightMatrix> currentGenerationReader = new PartitionedFileReader<>(
                CURRENT_GENERATION_FILE_PATTERN,
                ScoredWeightMatrix.SIZE_IN_BYTES,
                Serializer::deserializeScoredWeightMatrix);

        Writer<WeightMatrix> nextGenerationWriter = new PartitionedFileWriter<>(
                NEXT_GENERATION_FILE_PATTERN,
                MATRICES_PER_OUTPUT_FILE,
                Serializer::serializeWeightMatrix);

        Writer<ScoredWeightMatrix> survivorsWriter = new FileWriter<>(
                IO.getFile(nextFileName(SURVIVORS_FILE_PATTERN)),
                Serializer::serializeScoredWeightMatrix);

        Writer<Float> winRatioWriter = new FileWriter<>(
                IO.getFile(WIN_RATIO_FILE),
                floatValue -> ByteBuffer.wrap(new byte[Float.BYTES]).putFloat(floatValue).array(),
                true);

        Runnable currentGenerationRemover = () -> asStream(CURRENT_GENERATION_FILE_PATTERN).forEach(File::delete);

        Supplier<Integer> matrixMaxAbsWeightGenerator = new IntGeneratorFromString(MATRIX_MAX_ABS_WEIGHT_STRING);

        new Trainer(
                currentGenerationReader,
                nextGenerationWriter,
                survivorsWriter,
                winRatioWriter,
                currentGenerationRemover,
                SURVIVORS,
                CROSSINGOVER_INDIVIDUALS,
                MUTATED_COPIES,
                MUTATION_PERCENTAGE,
                POPULATION,
                matrixMaxAbsWeightGenerator)
                .run();
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

    private void printCurrentGenerationFile() throws IOException {
        if (!PartitionedFiles.exists(CURRENT_GENERATION_FILE_PATTERN)) {
            log.println("There was no input files with pattern '%s' found.", CURRENT_GENERATION_FILE_PATTERN);
            return;
        }

        log.println(" index\t| win %%\t\t| average energy diff");

        Reader<Score> reader = new PartitionedFileReader<>(CURRENT_GENERATION_FILE_PATTERN,
                ScoredWeightMatrix.SIZE_IN_BYTES, Serializer::deserializeScoreFromScoredWeightMatrix);
        reader = sortMaxValues(reader, SCORED_WEIGHT_MATRICES_TO_SHOW, Score::compareTo);

        printScores(reader);
    }

    private void printScores(Reader<Score> reader) throws IOException {
        Supplier<Integer> indexGenerator = new ScoredWeightMatrixIndexGenerator();
        int previousIndex = -1;
        int index = indexGenerator.get();
        while (reader.skip(index - previousIndex - 1)) {
            Score score = reader.read();
            log.println(" %d\t| %.2f%%\t| %.2f", index, score.getWinRate() * 100, score.getAverageEnergyDiff());
            previousIndex = index;
            index = indexGenerator.get();
        }
        reader.close();
    }

    private void printWinRatioFile() throws IOException {

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
            log.println("Population is the same as the mutated descendants number.\nSo, the mutated descendants will be used as the next generation.\n");
        } else if (POPULATION > mutatedDescendants) {
            log.println("Population (P = %d) is greater than the mutated descendants number (MD = %d).\n" +
                        "So, there will random individuals added (A) to the next generation A = P - MD", POPULATION, mutatedDescendants);
            log.println("A = %d (added random individuals)\n", POPULATION - mutatedDescendants);
        } else {
            log.println("Population (P = %d) is less than the mutated descendants number (MD = %d).\n" +
                        "So, some of the mutated descendants will be removed (R) from the next generation R = MD - P", POPULATION, mutatedDescendants);
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
            log.println("Population is the same as the mutated descendants number.\nSo, the mutated descendants will be used as the next generation.\n");
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
}
