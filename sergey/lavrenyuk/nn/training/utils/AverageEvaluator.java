package sergey.lavrenyuk.nn.training.utils;

public class AverageEvaluator {

    private float summ = 0f;
    private int count = 0;

    public AverageEvaluator put(float value) {
        summ += value;
        count++;
        return this;
    }

    public float getAverage() {
        return summ / count;
    }
}
