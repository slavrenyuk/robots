package sergey.lavrenyuk.test;

import sergey.lavrenyuk.event.BattleEnded;
import sergey.lavrenyuk.event.EnemyStatus;
import sergey.lavrenyuk.event.PerceptronStatus;
import sergey.lavrenyuk.event.RoundEnded;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.event.TurnStarted;
import sergey.lavrenyuk.module.PerceptronModule;
import sergey.lavrenyuk.module.WinPercentageScoreModule;
import sergey.lavrenyuk.nn.scoring.RoundResultConsumer;
import sergey.lavrenyuk.nn.scoring.Score;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertEqualsWithDelta;

public class TestWinPercentageScoreModule {

    public static void main(String[] args) {
        Runner.runTests(TestWinPercentageScoreModule.class);
    }

    public void testRoundResultNotReported() {
        final float maxRobotEnergy = 100;
        final List<Score.RoundResult> roundResults = new ArrayList<>();

        RoundResultConsumer roundResultConsumer = new RoundResultConsumer() {
            @Override
            public void accept(Score.RoundResult roundResult) {
                roundResults.add(roundResult);
            }

            @Override
            public void close() {}
        };
        PerceptronModule winPercentageScoreModule = new WinPercentageScoreModule(roundResultConsumer, maxRobotEnergy);
        winPercentageScoreModule.dispatch(new RoundStarted(null));
        // RoundEnded event was not issued, thus round result was not reported
        assertCondition(roundResults.isEmpty());

        // dummy result is issued on the next round start
        winPercentageScoreModule.dispatch(new RoundStarted(null));
        assertCondition(roundResults.size() == 1);
        assertCondition(!roundResults.get(0).isWin());
        assertEqualsWithDelta(roundResults.get(0).getEnergyDiff(), -maxRobotEnergy);
    }

    public void testHappyPath() {

        final double maxRobotEnergy = 100;
        final List<Score.RoundResult> roundResults = new ArrayList<>();
        final AtomicInteger closed = new AtomicInteger(0);

        RoundResultConsumer roundResultConsumer = new RoundResultConsumer() {
            @Override
            public void accept(Score.RoundResult roundResult) {
                roundResults.add(roundResult);
            }

            @Override
            public void close() {
                closed.incrementAndGet();
            }
        };
        PerceptronModule winPercentageScoreModule = new WinPercentageScoreModule(roundResultConsumer, maxRobotEnergy);

        winPercentageScoreModule.dispatch(new RoundStarted(null));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(90), buildEnemyStatus(80)));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(70), null /* previous value is used */));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(20), buildEnemyStatus(30)));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(0), null /* previous value is used */));
        winPercentageScoreModule.dispatch(new RoundEnded(false));
        assertCondition(roundResults.size() == 1);
        assertCondition(!roundResults.get(0).isWin());
        assertEqualsWithDelta(roundResults.get(0).getEnergyDiff(), -30);
        assertCondition(closed.get() == 0);

        winPercentageScoreModule.dispatch(new RoundStarted(null));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(80), buildEnemyStatus(90)));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(50), null /* previous value is used */));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(40), buildEnemyStatus(20)));
        winPercentageScoreModule.dispatch(new TurnStarted(buildPerceptronStatus(40), buildEnemyStatus(0)));
        winPercentageScoreModule.dispatch(new RoundEnded(true));
        assertCondition(roundResults.size() == 2);
        assertCondition(roundResults.get(1).isWin());
        assertEqualsWithDelta(roundResults.get(1).getEnergyDiff(), 40);
        assertCondition(closed.get() == 0);

        winPercentageScoreModule.dispatch(new BattleEnded());
        assertCondition(closed.get() == 1);
    }

    private static PerceptronStatus buildPerceptronStatus(int energy) {
        return new PerceptronStatus(energy, 0, 0, 0, 0, 0, 0, 0);
    }

    private static EnemyStatus buildEnemyStatus(int energy) {
        return new EnemyStatus(energy, 0, 0, 0, 0);
    }
}
