package sergey.lavrenyuk.test.base;

public class TestMetric {

    private int successful = 0;
    private int failed = 0;

    public void incrementSuccessful() {
        successful++;
    }

    public void incrementFailed() {
        failed++;
    }

    public TestMetric merge(TestMetric other) {
        TestMetric result = new TestMetric();
        result.successful = this.successful + other.successful;
        result.failed = this.failed + other.failed;
        return result;
    }

    @Override
    public String toString() {
        return String.format("tests: %d, successful: %d, failed: %d", successful + failed, successful, failed);
    }
}
