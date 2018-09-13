package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.training.TrainerRunner;
import sergey.lavrenyuk.test.base.BaseTest;

public class TestTrainerRunner extends BaseTest {

    public static void main(String[] args) {
        new TestTrainerRunner().runTests();
    }

    public void testPrintRemovedIndividuals() {
        TrainerRunner.printTrainingEstimates(10, 2, 3, 150);
    }

    public void testPrintAddedRandomIndividuals() {
        TrainerRunner.printTrainingEstimates(20, 4, 0, 200);
    }

    public void testPrintExactPopulation() {
        TrainerRunner.printTrainingEstimates(12, 1, 5, 204);
    }
}
