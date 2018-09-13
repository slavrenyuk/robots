package sergey.lavrenyuk.test.base;

import sergey.lavrenyuk.test.TestIntGeneratorFromString;
import sergey.lavrenyuk.test.TestMaxValuesStorage;
import sergey.lavrenyuk.test.TestNeuralNetwork;
import sergey.lavrenyuk.test.TestNeuralNetworkMode;
import sergey.lavrenyuk.test.TestPartitionedFileReader;
import sergey.lavrenyuk.test.TestPartitionedFileWriter;
import sergey.lavrenyuk.test.TestPartitionedFiles;
import sergey.lavrenyuk.test.TestSerializer;
import sergey.lavrenyuk.test.TestTrainerRunner;
import sergey.lavrenyuk.test.TestWeightMatrix;
import sergey.lavrenyuk.test.TestWeightMatrixScorer;
import sergey.lavrenyuk.test.TestWeightMatrixScorerRawDataIO;

import java.util.Arrays;
import java.util.List;

public class AllTestsRunner {

    private static final List<Class<? extends BaseTest>> TEST_CLASSES = Arrays.asList(
            TestNeuralNetwork.class,
            TestNeuralNetworkMode.class,
            TestIntGeneratorFromString.class,
            TestWeightMatrix.class,
            TestWeightMatrixScorer.class,
            TestWeightMatrixScorerRawDataIO.class,
            TestPartitionedFiles.class,
            TestSerializer.class,
            TestMaxValuesStorage.class,
            TestPartitionedFileReader.class,
            TestPartitionedFileWriter.class,
            TestTrainerRunner.class
    );

    public static void main(String[] args) {
        TestMetric totalMetric = new TestMetric();
        for (Class<? extends BaseTest> testClass : TEST_CLASSES) {
            System.out.println(testClass.getName());
            try {
                BaseTest test = testClass.newInstance();
                totalMetric = totalMetric.merge(test.runTests());
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("====================================================================================");
        }
        System.out.println("Total " + totalMetric);
    }
}
