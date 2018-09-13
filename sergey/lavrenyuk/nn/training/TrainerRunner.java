package sergey.lavrenyuk.nn.training;

import sergey.lavrenyuk.MyRobot;
import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Log;
import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.IntGeneratorFromString;
import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.score.ScoredWeightMatrix;
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

import static sergey.lavrenyuk.io.PartitionedFiles.findNextAvailableFileName;
import static sergey.lavrenyuk.io.PartitionedFiles.FileIterator;
import static sergey.lavrenyuk.io.PartitionedFiles.resolvePlaceholder;
import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.verifyInput;

public class TrainerRunner {

    private static final Log log;
    private static final Scanner scanner;

    static { // initialize IO with our robot base directory
        String robotClassName = MyRobot.class.getSimpleName();
        File baseFolder = new File(MyRobot.class.getResource(".").getPath() + robotClassName + ".data");
        String basePath = baseFolder.getAbsolutePath() + "/";

        IO.initialize(() -> System.out, fileName -> new File(basePath + fileName));

        log = new Log(TrainerRunner.class);
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) throws IOException{

        log.println("Choose one of the next options:");
        log.println("T - training");
        log.println("E - print training estimates");
        log.println("W - print win percentage");
        log.print("Input: ");

        String input = scanner.nextLine();

        if ("T".equalsIgnoreCase(input)) {
            File firstInputFile = new File(resolvePlaceholder(Config.getString("trainer.inputFilePattern"), 0));
            if (!firstInputFile.exists()) {
                performInitialTraining();
            } else {
                performTraining();
            }
        } else if ("E".equalsIgnoreCase(input)) {
                printTrainingEstimates();
        } else if ("W".equalsIgnoreCase(input)) {
            printWinRatioFile();
        } else {
            log.println("Invalid input");
        }
    }

    public static void performInitialTraining() throws IOException {
        RandomWeightMatrixGenerator randomWeightMatrixGenerator = new RandomWeightMatrixGenerator();

        Supplier<Integer> matrixMaxAbsWeightGenerator =
                new IntGeneratorFromString(Config.getString("neuralNetwork.matrixMaxAbsWeight", "1"));

        Writer<WeightMatrix> initialGenerationWriter = new PartitionedFileWriter<>(
                Config.getString("trainer.outputFilePattern"),
                Config.getInteger("trainer.matricesPerOutputFile"),
                Serializer::serializeWeightMatrix);

        final int population = Config.getInteger("trainer.population");
        for (int i = 0; i < population; i++) {
            initialGenerationWriter.write(randomWeightMatrixGenerator.next(matrixMaxAbsWeightGenerator.get()));
        }
        initialGenerationWriter.close();
    }

    public static void performTraining() throws IOException {
        printTrainingEstimates(
                Config.getInteger("trainer.survivors"),
                Config.getInteger("trainer.crossingoverIndividuals"),
                Config.getInteger("trainer.mutatedCopies"),
                Config.getInteger("trainer.population"));

        log.println("\nConfirm and continue? Y/N\n");
        String input = scanner.nextLine();
        if (!"Y".equalsIgnoreCase(input)) {
            return;
        }

        Reader<ScoredWeightMatrix> currentGenerationReader = new PartitionedFileReader<>(
                Config.getString("trainer.inputFilePattern"),
                ScoredWeightMatrix.SIZE_IN_BYTES,
                Serializer::deserializeScoredWeightMatrix);

        Writer<WeightMatrix> nextGenerationWriter = new PartitionedFileWriter<>(
                Config.getString("trainer.outputFilePattern"),
                Config.getInteger("trainer.matricesPerOutputFile"),
                Serializer::serializeWeightMatrix);

        Writer<WeightMatrix> survivorsWriter = new FileWriter<>(
                findNextAvailableFileName(Config.getString("trainer.survivorsFilePattern")),
                Serializer::serializeWeightMatrix);

        Writer<Float> winRatioWriter = new FileWriter<>(
                Config.getString("trainer.winRatioFile"),
                floatValue -> ByteBuffer.wrap(new byte[Float.BYTES]).putFloat(floatValue).array(),
                true);

        Runnable currentGenerationRemover = () -> {
            FileIterator fileIterator = new FileIterator(Config.getString("trainer.inputFilePattern"));
            while (fileIterator.hasNext()) {
                fileIterator.next().delete();
            }
        };

        int survivors = Config.getInteger("trainer.survivors");
        int crossingoverIndividuals = Config.getInteger("trainer.crossingoverIndividuals");
        int mutatedCopies = Config.getInteger("trainer.mutatedCopies");
        int mutationPercentage = Config.getInteger("trainer.mutationPercentage");
        int population = Config.getInteger("trainer.population");

        Supplier<Integer> matrixMaxAbsWeightGenerator =
                new IntGeneratorFromString(Config.getString("neuralNetwork.matrixMaxAbsWeight", "1"));

        new Trainer(
                currentGenerationReader,
                nextGenerationWriter,
                survivorsWriter,
                winRatioWriter,
                currentGenerationRemover,
                survivors,
                crossingoverIndividuals,
                mutatedCopies,
                mutationPercentage,
                population,
                matrixMaxAbsWeightGenerator)
                .run();
    }

    public static void printWinRatioFile() throws IOException {

        File winRatioFile = IO.getFile(Config.getString("trainer.winRatioFile"));

        if (!winRatioFile.exists()) {
            log.println("Win ratio file %s not found", winRatioFile.getAbsolutePath());
            return;
        }
        if (!winRatioFile.isFile()) {
            log.println(" %s is not a file", winRatioFile.getAbsolutePath());
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
            log.println("Generation %d survivors wins in %.4f%% of rounds, population wins in %.4f%% of rounds",
                    generation, populationAverageWinRatio * 100, survivorsAverageWinRatio * 100);
        }
    }

    public static void printTrainingEstimates() {
        printTrainingEstimates(
                Config.getInteger("trainer.survivors"),
                Config.getInteger("trainer.crossingoverIndividuals"),
                Config.getInteger("trainer.mutatedCopies"),
                Config.getInteger("trainer.population"));
    }

    public static void printTrainingEstimates(final int survivors,
                                              final int crossingoverIndividuals,
                                              final int mutatedCopies,
                                              final int population) {
        verifyInput(survivors, crossingoverIndividuals, mutatedCopies, population);

        log.println("Input parameters:");
        log.println("S = %d\t(survivors)\nC = %d\t(crossingover individuals)\nM = %d\t(mutated copies)\nP = %d\t(population)\n",
                survivors, crossingoverIndividuals, mutatedCopies, population);
        log.println("Crossingover individuals are chosen among top rated survivors and will be paired with another survivors.");

        final int crossingovePairs = crossingoverIndividuals * survivors - crossingoverIndividuals * (1 + crossingoverIndividuals) / 2;
        log.println("Number of crossingover pairs CP = (S - 1) + (S - 2) + ... + (S - C) = C * S - C * (1 + C) / 2");
        log.println("CP = %d (crossingover pairs)\n", crossingovePairs);

        final int descendants = survivors + crossingovePairs * 2;
        log.println("Each crossingover pair produces 2 children.");
        log.println("Let's call survivors and their children as descendants D = S + 2 * CP");
        log.println("D = %d (descendants)\n", descendants);

        final int mutatedDescendants = descendants * (1 + mutatedCopies);
        log.println("After that, descendants are cloned and mutated M = %d times", mutatedCopies);
        log.println("Let's call descendants and their clones as mutated descendants MD = D * (1 + M)");
        log.println("MD = %d (mutated descendants)\n", mutatedDescendants);

        if (population == mutatedDescendants) {
            log.println("Population is the same as the mutated descendants number.\nSo, the mutated descendants will be used as the next generation.");
        } else if (population > mutatedDescendants) {
            log.println("Population (P = %d) is greater than the mutated descendants number (MD = %d).\n" +
                    "So, there will random individuals added (A) to the next generation A = P - MD", population, mutatedDescendants);
            log.println("A = %d (added random individuals)", population - mutatedDescendants);
        } else {
            log.println("Population (P = %d) is less than the mutated descendants number (MD = %d).\n" +
                    "So, some of the mutated descendants will be removed (R) from the next generation R = MD - P", population, mutatedDescendants);
            log.println("R = %d (removed individuals)", mutatedDescendants - population);
        }
        log.println("\n");
    }
}
