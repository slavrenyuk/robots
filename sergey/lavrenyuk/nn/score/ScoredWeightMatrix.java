package sergey.lavrenyuk.nn.score;

import sergey.lavrenyuk.nn.WeightMatrix;

import java.util.Objects;

public class ScoredWeightMatrix {

    public static final int SIZE_IN_BYTES = Score.SIZE_IN_BYTES + WeightMatrix.SIZE_IN_BYTES;

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
