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

    public void add(TestMetric other) {
        this.successful += other.successful;
        this.failed += other.failed;
    }

    @Override
    public String toString() {
        return String.format("tests: %d, successful: %d, failed: %d", successful + failed, successful, failed);
    }
}
