package sergey.lavrenyuk.module;

import sergey.lavrenyuk.event.BattleEnded;
import sergey.lavrenyuk.event.PerceptronEvent;
import sergey.lavrenyuk.event.RoundEnded;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.io.Log;

public class CriticalFailureModule implements PerceptronModule {

    // maximum number of rounds ended with nonrecoverable exception for robot to get disabled
    // we will anyway get sporadic exceptions, e.g. the robot thread may get stuck because of too long garbage collection
    public static final int ROUNDS_FAILED_THRESHOLD = 20;

    // number of rounds ended with nonrecoverable exception
    private volatile int roundsFailed = 0;

    // initially true, will be set to false in the run() method
    // is checked at the beginning of each round to indicate that previous round has failed. unfortunately there is no other way
    // to detect round failure, since robot thread is usually get stuck and forced stop on nonrecoverable exception
    private volatile boolean roundEnded = true;

    @Override
    public void dispatch(PerceptronEvent event) {
        if (event instanceof RoundStarted) {
            onRoundStarted((RoundStarted) event);
        } else if (event instanceof RoundEnded) {
            onRoundEnded((RoundEnded) event);
        } else if (event instanceof BattleEnded) {
            onBattleEnded((BattleEnded) event);
        }
    }

    public void onRoundStarted(RoundStarted event) {
        if (!roundEnded) {
            roundsFailed++;
            if (roundsFailed >= ROUNDS_FAILED_THRESHOLD) {
                event.getRobotControl().disable();
            }
        }
        roundEnded = false;
    }

    public void onRoundEnded(RoundEnded event) {
        roundEnded = true;
    }

    public void onBattleEnded(BattleEnded event) {
        Log log = new Log(CriticalFailureModule.class);
        if (roundsFailed == 0) {
            log.info("No rounds failed with a nonrecoverable exception.");
        } else {
            log.error(String.format("Totally %d rounds failed with a nonrecoverable exception.", roundsFailed));
        }
    }
}
