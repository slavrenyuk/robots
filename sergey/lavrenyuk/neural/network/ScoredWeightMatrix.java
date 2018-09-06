package sergey.lavrenyuk.neural.network;

import java.util.Objects;

public class ScoredWeightMatrix {

    private final Score score;
    private final WeightMatrix weightMatrix;

    public ScoredWeightMatrix(Score score, WeightMatrix weightMatrix) {
        this.score = score;
        this.weightMatrix = weightMatrix;
    }

    public Score getScore() {
        return score;
    }


    public WeightMatrix getWeightMatrix() {
        return weightMatrix;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ScoredWeightMatrix that = (ScoredWeightMatrix) obj;
        return Objects.equals(score, that.score)
                && Objects.equals(weightMatrix, that.weightMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, weightMatrix);
    }
}
