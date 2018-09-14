package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.training.utils.AverageEvaluator;

import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithDelta;

public class TestAverageEvaluator {

    public static void main(String[] args) {
        Runner.runTests(TestAverageEvaluator.class);
    }

    public void test() {
        AverageEvaluator averageEvaluator = new AverageEvaluator();
        averageEvaluator.put(1f);
        averageEvaluator.put(2f);
        averageEvaluator.put(0f);
        averageEvaluator.put(3f);
        averageEvaluator.put(-1f);
        assertEqualsWithDelta(averageEvaluator.getAverage(), 1f);
    }
}
