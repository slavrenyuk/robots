package sergey.lavrenyuk.io.data;

import sergey.lavrenyuk.nn.ScoredWeightMatrix;
import sergey.lavrenyuk.nn.WeightMatrix;

public interface WeightMatrixScorer extends Reader<WeightMatrix>, Writer<ScoredWeightMatrix> {

    boolean isWriteExpected();

    WeightMatrix getCurrentMatrix();
}
