package sergey.lavrenyuk.io.data;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.nn.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.IOException;

public class WeightMatrixIO {

    private final DataIO weightMatrixDataIO;
    private final int roundsPerMatrix;

    private WeightMatrix weightMatrix;
    private int roundsWithCurrentMatrix;

    public WeightMatrixIO(DataIO weightMatrixDataIO, int roundsPerMatrix) {
        this.weightMatrixDataIO = weightMatrixDataIO;
        this.roundsPerMatrix = roundsPerMatrix;
        this.roundsWithCurrentMatrix = 0;
        try {
            this.weightMatrix = Serializer.deserializeWeightMatrix(this.weightMatrixDataIO.read());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isWriteExpected() {
        return roundsWithCurrentMatrix == roundsPerMatrix;
    }

    public WeightMatrix read() {
        if (isWriteExpected()) {
            throw new IllegalStateException(
                    String.format("Write call is expected. roundsPerMatrix = %d, roundsWithCurrentMatrix = %d",
                            roundsPerMatrix, roundsWithCurrentMatrix));
        }
        if (roundsWithCurrentMatrix < roundsPerMatrix) {
            roundsWithCurrentMatrix++;
            return weightMatrix;
        }

        try {
            weightMatrix = Serializer.deserializeWeightMatrix(weightMatrixDataIO.read());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        roundsWithCurrentMatrix = 1;
        return weightMatrix;
    }

    public void write(ScoredWeightMatrix scoredWeightMatrix) {
        if (!isWriteExpected()) {
            throw new IllegalStateException(
                    String.format("Write call is not expected. roundsPerMatrix = %d, roundsWithCurrentMatrix = %d",
                            roundsPerMatrix, roundsWithCurrentMatrix));
        }

        try {
            weightMatrixDataIO.write(Serializer.serializeScoredWeightMatrix(scoredWeightMatrix));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
