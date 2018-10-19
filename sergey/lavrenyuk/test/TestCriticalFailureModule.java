package sergey.lavrenyuk.test;

import sergey.lavrenyuk.event.BattleEnded;
import sergey.lavrenyuk.event.RoundEnded;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.module.CriticalFailureModule;
import sergey.lavrenyuk.module.PerceptronModule;
import sergey.lavrenyuk.test.base.PerceptronTestControl;

import java.util.Random;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;

public class TestCriticalFailureModule {

    public static void main(String[] args) {
        Runner.runTests(TestCriticalFailureModule.class);
    }

    public void testCriticalFailureModule() {

        Random random = new Random();
        PerceptronModule criticalFailureModule = new CriticalFailureModule();

        final int successfulRounds = 10;
        for (int i = 0; i < successfulRounds; i++) {
            PerceptronTestControl control = new PerceptronTestControl();
            criticalFailureModule.dispatch(new RoundStarted(control));
            criticalFailureModule.dispatch(new RoundEnded(random.nextBoolean()));
            assertCondition(!control.isDisabled());
        }

        for (int i = 0; i < CriticalFailureModule.ROUNDS_FAILED_THRESHOLD; i++) {
            PerceptronTestControl control = new PerceptronTestControl();
            criticalFailureModule.dispatch(new RoundStarted(control));
            // no RoundEnded event is issued
            assertCondition(!control.isDisabled());
        }

        final int severalAdditionalFailedRounds = 3;
        for (int i = 0; i < severalAdditionalFailedRounds; i++) {
            PerceptronTestControl control = new PerceptronTestControl();
            criticalFailureModule.dispatch(new RoundStarted(control));
            // no RoundEnded event is issued
            assertCondition(control.isDisabled());
        }

        // round failure is detected on the next round start, so the latest failure will not be counted
        System.out.println(String.format("ERROR: CriticalFailureModule - Totally %d rounds failed with a nonrecoverable exception. " +
                "This message is expected below.", CriticalFailureModule.ROUNDS_FAILED_THRESHOLD + severalAdditionalFailedRounds - 1));
        criticalFailureModule.dispatch(new BattleEnded());
    }
}
