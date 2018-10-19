package sergey.lavrenyuk.event;

import java.util.Optional;

public class TurnStarted implements PerceptronEvent {

    private final PerceptronStatus perceptronStatus;
    private final EnemyStatus enemyStatus;

    public TurnStarted(PerceptronStatus perceptronStatus, EnemyStatus enemyStatus) {
        this.perceptronStatus = perceptronStatus;
        this.enemyStatus = enemyStatus;
    }

    public PerceptronStatus getPerceptronStatus() {
        return perceptronStatus;
    }

    public Optional<EnemyStatus> getEnemyStatus() {
        return Optional.ofNullable(enemyStatus);
    }
}
