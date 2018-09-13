package sergey.lavrenyuk.nn.training;

import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.RandomWeightMatrixGenerator;
import sergey.lavrenyuk.nn.WeightMatrix;
import sergey.lavrenyuk.nn.score.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.training.utils.AverageEvaluator;
import sergey.lavrenyuk.nn.training.utils.MaxValuesStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.concatLazily;
import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.crossingover;
import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.mutateLazily;
import static sergey.lavrenyuk.nn.training.utils.TrainerUtils.verifyInput;

public class Trainer {
    private final Reader<ScoredWeightMatrix> currentGenerationReader;
    private final Writer<WeightMatrix> nextGenerationWriter;
    private final Writer<WeightMatrix> survivorsWriter;
    private final Writer<Float> winRatioWriter;
    private final Runnable currentGenerationRemover;
    private final int survivors;
    private final int crossingoverIndividuals;
    private final int mutatedCopies;
    private final int mutationPercentage;
    private final int population;
    private final Supplier<Integer> matrixMaxAbsWeightGenerator;

    public Trainer(Reader<ScoredWeightMatrix> currentGenerationReader,
                   Writer<WeightMatrix> nextGenerationWriter,
                   Writer<WeightMatrix> survivorsWriter,
                   Writer<Float> winRatioWriter,
                   Runnable currentGenerationRemover,
                   int survivors,
                   int crossingoverIndividuals,
                   int mutatedCopies,
                   int mutationPercentage,
                   int population,
                   Supplier<Integer> matrixMaxAbsWeightGenerator) {

        verifyInput(survivors, crossingoverIndividuals, mutatedCopies, mutationPercentage, population);

        this.currentGenerationReader = currentGenerationReader;
        this.nextGenerationWriter = nextGenerationWriter;
        this.survivorsWriter = survivorsWriter;
        this.winRatioWriter = winRatioWriter;
        this.currentGenerationRemover = currentGenerationRemover;
        this.survivors = survivors;
        this.crossingoverIndividuals = crossingoverIndividuals;
        this.mutatedCopies = mutatedCopies;
        this.mutationPercentage = mutationPercentage;
        this.population = population;
        this.matrixMaxAbsWeightGenerator = matrixMaxAbsWeightGenerator;
    }

    public void run() throws IOException {

        MaxValuesStorage<ScoredWeightMatrix> storage = new MaxValuesStorage<>(
                survivors, Comparator.comparing(ScoredWeightMatrix::getScore));
        AverageEvaluator averageEvaluator = new AverageEvaluator();
        ScoredWeightMatrix scoredWeightMatrix;
        while ((scoredWeightMatrix = currentGenerationReader.read()) != null) {
            storage.put(scoredWeightMatrix);
            averageEvaluator.put(scoredWeightMatrix.getScore().getWinRate());
        }
        List<ScoredWeightMatrix> survivorsScoredWeightMatrices = storage.asList();
        float populationAverageWinRatio = averageEvaluator.getAverage();

        List<WeightMatrix> survivorsWeightMatrices = new ArrayList<>();
        averageEvaluator = new AverageEvaluator();
        for (ScoredWeightMatrix survivorScoredWeightMatrix : survivorsScoredWeightMatrices) {
            survivorsWriter.write(survivorScoredWeightMatrix.getWeightMatrix());
            survivorsWeightMatrices.add(survivorScoredWeightMatrix.getWeightMatrix());
            averageEvaluator.put(survivorScoredWeightMatrix.getScore().getWinRate());
        }
        float survivorsAverageWinRatio = averageEvaluator.getAverage();

        winRatioWriter.write(populationAverageWinRatio);
        winRatioWriter.write(survivorsAverageWinRatio);

        currentGenerationReader.close();
        survivorsWriter.close();
        winRatioWriter.close();

        currentGenerationRemover.run();

        List<WeightMatrix> childrenWeightMatrices = crossingover(survivorsWeightMatrices, crossingoverIndividuals);
        Iterable<WeightMatrix> survivorsAndTheirChildren = concatLazily(survivorsWeightMatrices, childrenWeightMatrices);
        Iterable<WeightMatrix> mutatedSurvivorsAndTheirChildren = mutateLazily(survivorsAndTheirChildren, mutatedCopies, mutationPercentage);

        int written = 0;
        for (WeightMatrix weightMatrix : concatLazily(survivorsAndTheirChildren, mutatedSurvivorsAndTheirChildren)) {
            written++;
            nextGenerationWriter.write(weightMatrix);
            if (written == population) {
                break;
            }
        }

        RandomWeightMatrixGenerator randomWeightMatrixGenerator = new RandomWeightMatrixGenerator();
        for (; written < population; written++) {
            nextGenerationWriter.write(randomWeightMatrixGenerator.next(matrixMaxAbsWeightGenerator.get()));
        }

        nextGenerationWriter.close();
    }
}
