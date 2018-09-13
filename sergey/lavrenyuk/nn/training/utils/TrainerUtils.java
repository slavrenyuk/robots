package sergey.lavrenyuk.nn.training.utils;

import sergey.lavrenyuk.nn.WeightMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TrainerUtils {

    private TrainerUtils() {}

    public static void verifyInput(int survivors,
                                   int crossingoverIndividuals,
                                   int mutatedCopies,
                                   int mutationPercentage,
                                   int population) {
        verifyInput(survivors, crossingoverIndividuals, mutatedCopies, population);
        if (mutationPercentage < 0 || mutationPercentage > 100) {
            throw new IllegalArgumentException("mutation percentage must be greater or equal to 0 and less or equal to 100");
        }
    }

    public static void verifyInput(int survivors,
                                   int crossingoverIndividuals,
                                   int mutatedCopies,
                                   int population) {
        if (population < 1) {
            throw new IllegalArgumentException("population must be greater than 1");
        }
        if (crossingoverIndividuals < 1) {
            throw new IllegalArgumentException("crossingover individuals must be greater than 1");
        }
        if (survivors < crossingoverIndividuals) {
            throw new IllegalArgumentException("crossingover individuals are chosen from survivals, " +
                    "so number of crossingover individuals must be less or equal to survivors number");
        }
        if (mutatedCopies < 0) {
            throw new IllegalArgumentException("mutated copies must be greater than 0");
        }
    }

    @SafeVarargs
    public static <T> Iterable<T> concatLazily(Iterable<T>... iterables) {
        if (iterables == null || iterables.length == 0) {
            throw new IllegalArgumentException("No iterables found");
        }

        return () -> new Iterator<T>() {

            int index = 0;
            Iterator<T> it = iterables[++index].iterator();

            @Override
            public boolean hasNext() {
                if (it.hasNext()) {
                    return true;
                }
                if (index < iterables.length) {
                    it = iterables[++index].iterator();
                    return hasNext();
                }
                return false;
            }

            @Override
            public T next() {
                if (it.hasNext()) {
                    return it.next();
                }
                if (index < iterables.length) {
                    it = iterables[++index].iterator();
                    return next();
                }
                return null;
            }
        };
    }

    public static Iterable<WeightMatrix> mutateLazily(Iterable<WeightMatrix> weightMatrices, int mutatedCopies, int mutationPercentage) {
        if (!weightMatrices.iterator().hasNext()) {
            return Collections.emptyList();
        }

        final WeightMatrixMutator mutator = new WeightMatrixMutator(mutationPercentage);
        return () -> new Iterator<WeightMatrix>() {

            Iterator<WeightMatrix> originalIterator = weightMatrices.iterator();
            int mutatedCopyIndex = 1;

            @Override
            public boolean hasNext() {
                return originalIterator.hasNext() || mutatedCopyIndex < mutatedCopies;
            }

            @Override
            public WeightMatrix next() {
                if (originalIterator.hasNext()) {
                    return mutator.mutate(originalIterator.next());
                }
                if (mutatedCopyIndex < mutatedCopies) {
                    originalIterator = weightMatrices.iterator();
                    mutatedCopyIndex++;
                    // we checked that there is at least one element at the very beginning of this method
                    return originalIterator.next();
                }
                return null;
            }
        };
    }

    // this is not lazy for 2 reasons:
    // a) there won't be a lot of data
    // b) new WeightMatrix instances are created as a result of crossingover; if we return a lazy result, the same matrix
    //      will be created several times because we are going to iterate over the result multiple times during mutation
    public static List<WeightMatrix> crossingover(List<WeightMatrix> weightMatrices, int crossingoverIndividuals) {
        List<WeightMatrix> result = new ArrayList<>();
        for (int i = 0; i < crossingoverIndividuals; i++) {
            for (int j = i + 1; j < weightMatrices.size(); j++) {
                result.add(new WeightMatrix(
                        weightMatrices.get(i).getInputToHiddenWeights(),
                        weightMatrices.get(j).getHiddenToOutputWeights()));
                result.add(new WeightMatrix(
                        weightMatrices.get(j).getInputToHiddenWeights(),
                        weightMatrices.get(i).getHiddenToOutputWeights()));
            }
        }
        return result;
    }
}
