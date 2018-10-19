package sergey.lavrenyuk.module;

import sergey.lavrenyuk.event.BattleEnded;
import sergey.lavrenyuk.event.PerceptronEvent;
import sergey.lavrenyuk.event.RoundEnded;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.event.TurnStarted;
import sergey.lavrenyuk.nn.scoring.RoundResultConsumer;
import sergey.lavrenyuk.nn.scoring.Score;

public class WinPercentageScoreModule implements PerceptronModule {

    private final RoundResultConsumer roundResultConsumer;

    private final double maxRobotEnergy;

    private volatile double perceptronEnergy;

    private volatile double enemyEnergy;

    // initially true, will be set to false in the run() method
    // is checked at the beginning of each round to indicate that previous round has failed. unfortunately there is no other way
    // to detect round failure, since robot thread is usually get stuck and forced stop on nonrecoverable exception
    private volatile boolean roundResultReported = true;

    public WinPercentageScoreModule(RoundResultConsumer roundResultConsumer, double maxRobotEnergy) {
        this.roundResultConsumer = roundResultConsumer;
        this.maxRobotEnergy = maxRobotEnergy;
    }

    @Override
    public void dispatch(PerceptronEvent event) {
        if (event instanceof RoundStarted) {
            onRoundStarted((RoundStarted) event);
        } else if (event instanceof TurnStarted) {
            onTurnStarted((TurnStarted) event);
        } else if (event instanceof RoundEnded) {
            onRoundEnded((RoundEnded) event);
        } else if (event instanceof BattleEnded) {
            onBattleEnded((BattleEnded) event);
        }
    }

    public void onRoundStarted(RoundStarted event) {
        perceptronEnergy = maxRobotEnergy;
        enemyEnergy = maxRobotEnergy;
        // round result was not reported in the previous round
        // that may happen because of a nonrecoverable exception
        // at least we should report a dummy score to not break the whole process
        // there is a chance the we will fail during reporting, in that case there is nothing we can do
        // also this will not help if we failed to report during the last round
        if(!roundResultReported) {
            roundResultConsumer.accept(new Score.RoundResult(false, (float) -maxRobotEnergy));
        }
        roundResultReported = false;
    }

    public void onTurnStarted(TurnStarted event) {
        perceptronEnergy = event.getPerceptronStatus().getEnergy();
        // enemyStatus == null means no enemy was scanned this turn
        if (event.getEnemyStatus().isPresent()) {
            enemyEnergy = event.getEnemyStatus().get().getEnergy();
        }
    }

    public void onRoundEnded(RoundEnded event) {
        roundResultConsumer.accept(new Score.RoundResult(
                event.isVictory(),
                (float) (event.isVictory() ? perceptronEnergy : - enemyEnergy)));
        roundResultReported = true;
    }

    public void onBattleEnded(BattleEnded event) {
        roundResultConsumer.close();
    }
}
