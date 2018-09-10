package sergey.lavrenyuk.nn.score;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.data.Reader;
import sergey.lavrenyuk.io.data.Writer;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class WeightMatrixScorer implements Supplier<WeightMatrix>, RoundResultConsumer {

    private final Reader<byte[]> dataReader;
    private final Writer<byte[]> dataWriter;
    private final int roundsPerMatrix;

    private WeightMatrix currentMatrix;
    private Score.Builder currentMatrixScore;
    private int roundsWithCurrentMatrix;

    public static WeightMatrixScorer create(String inputFilePattern,
                                            String outputFilePattern,
                                            int startFileIndex,
                                            int roundsPerMatrix,
                                            boolean robocodeEnvironment) {
        WeightMatrixScorerRawDataIO rawDataIO;
        try {
             rawDataIO = new WeightMatrixScorerRawDataIO(inputFilePattern, outputFilePattern, WeightMatrix.SIZE_IN_BYTES,
                     ScoredWeightMatrix.SIZE_IN_BYTES, startFileIndex, robocodeEnvironment);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new WeightMatrixScorer(rawDataIO, rawDataIO, roundsPerMatrix);
    }

    public WeightMatrixScorer(Reader<byte[]> dataReader, Writer<byte[]> dataWriter, int roundsPerMatrix) {
        this.dataReader = dataReader;
        this.dataWriter = dataWriter;
        this.roundsPerMatrix = roundsPerMatrix;
        this.currentMatrix = read();
        this.currentMatrixScore = Score.builder();
        this.roundsWithCurrentMatrix = 0;
    }

    @Override
    public WeightMatrix get() {
        if (roundsWithCurrentMatrix < roundsPerMatrix) {
            roundsWithCurrentMatrix++;
            return currentMatrix;
        }
        roundsWithCurrentMatrix = 1;
        currentMatrix = read();
        return currentMatrix;
    }

    @Override
    public void accept(Score.RoundResult roundResult) {

        currentMatrixScore.addRoundResult(roundResult);

        if (roundsPerMatrix == roundsWithCurrentMatrix) {
            ScoredWeightMatrix scoredWeightMatrix = new ScoredWeightMatrix(currentMatrixScore.build(), currentMatrix);
            try {
                dataWriter.write(Serializer.serializeScoredWeightMatrix(scoredWeightMatrix));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                currentMatrixScore = Score.builder();
            }
        }
    }

    @Override
    public void close() {
        try {
            dataWriter.close();
            dataReader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private WeightMatrix read() {
        try {
            return Optional
                    .ofNullable(dataReader.read())
                    .map(Serializer::deserializeWeightMatrix)
                    .orElse(null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
