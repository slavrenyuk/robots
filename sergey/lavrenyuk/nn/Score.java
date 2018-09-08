package sergey.lavrenyuk.nn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Score implements Comparable<Score> {

    public static final int SIZE_IN_BYTES = 2 * Float.BYTES;

    private final float winRate;
    private final float averageEnergyDiff;

    public Score(float winRate, float averageEnergyDiff) {
        this.winRate = winRate;
        this.averageEnergyDiff = averageEnergyDiff;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float getWinRate() {
        return winRate;
    }

    public float getAverageEnergyDiff() {
        return averageEnergyDiff;
    }

    @Override
    public int compareTo(Score that) {
        int winRatesComparison = Double.compare(this.winRate, that.winRate);
        return (winRatesComparison != 0)
                ? winRatesComparison
                : Double.compare(this.averageEnergyDiff, that.averageEnergyDiff);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((Score) obj) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(winRate, averageEnergyDiff);
    }

    public static class Builder {

        private int wins = 0;
        private List<Float> energyDiffs = new ArrayList<>();

        public Builder addRoundResult(boolean win, float energyDiff) {
            if (win) {
                wins++;
            }
            energyDiffs.add(energyDiff);
            return this;
        }

        public Score build() {
            int rounds = energyDiffs.size();
            float totalEnergyDiff = 0.0f;
            for (double energyDiff : energyDiffs) {
                totalEnergyDiff += energyDiff;
            }
            return new Score((float) wins / rounds, totalEnergyDiff / rounds);
        }
    }
}
