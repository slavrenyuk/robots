package sergey.lavrenyuk.nn.score;

import sergey.lavrenyuk.io.Serializer;
import sergey.lavrenyuk.io.Reader;
import sergey.lavrenyuk.io.Writer;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.io.IOException;
import java.util.function.Supplier;

// TODO document this class doesn't support multiple threads in parallel, but it supports different thread sequential access,
// which may happen in the Robocode environment, observed when round ended because of too many skipped turns, which, in turn, presumably is caused by garbage collection
// in that case battle thread performs some manipulation over the robot code instead of the robot's thread
public class WeightMatrixScorer implements Supplier<WeightMatrix>, RoundResultConsumer {

    private final Reader<byte[]> dataReader;
    private final Writer<byte[]> dataWriter;
    private final int roundsPerMatrix;

    private volatile WeightMatrix currentMatrix;
    private volatile Score.Builder currentMatrixScore;
    private volatile int roundsWithCurrentMatrix;

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
    public synchronized WeightMatrix get() {
        if (roundsWithCurrentMatrix < roundsPerMatrix) {
            roundsWithCurrentMatrix++;
            return currentMatrix;
        }
        roundsWithCurrentMatrix = 1;
        currentMatrix = read();
        return currentMatrix;
    }

    @Override
    public synchronized void accept(Score.RoundResult roundResult) {

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
    public synchronized void close() {
        try {
            dataWriter.close();
            dataReader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private WeightMatrix read() {
        try {
            byte[] data = dataReader.read();
            return data.length != 0
                    ? Serializer.deserializeWeightMatrix(data)
                    : null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
