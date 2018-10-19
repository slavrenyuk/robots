package sergey.lavrenyuk;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.RoundEndedEvent;
import robocode.StatusEvent;
import sergey.lavrenyuk.event.PerceptronEvent;
import sergey.lavrenyuk.event.PerceptronEvents;
import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.Exceptions;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Log;
import sergey.lavrenyuk.module.CriticalFailureModule;
import sergey.lavrenyuk.module.NeuralNetworkModule;
import sergey.lavrenyuk.module.PerceptronModule;
import sergey.lavrenyuk.module.PerceptronControl;
import sergey.lavrenyuk.module.WinPercentageScoreModule;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// TODO document why everything is wrapped into try blocks
public class Perceptron extends AdvancedRobot implements PerceptronControl {

    // we have a limitation on initializing static and final fields
    // base robot class is fully initialized and set up only in the run() method
    private static List<PerceptronModule> modules;

    // up to 10 different exceptions will be saved and printed at the end of the battle
    private static final Exceptions exceptions = new Exceptions(10);

    // robot may be disabled because of to many round failures
    // that is done to mitigate cases when an unrecoverable exception causes all consecutive rounds to fail
    private static volatile boolean disabled = false;

    // need to have this flag since we can not fully initialize robot in a constructor,
    // but only in a run() method and first onStatus event is issued before run() method
    private volatile boolean roundStarted = false;

    // console logger
    private Log log;

    /**
     * This method is called once at the beginning of each round. It is an entry point of the robot's logic.
     * Here we have base robot class fully initialized and set up, not in the class constructor as we get used in the Java world.
     * Keep in mind that first {@link #onStatus(StatusEvent)} happens before this method is called.
     */
    @Override
    public synchronized void run() {
        try {

            if (disabled) { // do not do anything if the robot is disabled because of too many unrecoverable exceptions
                return;
            }

            // IO has to be initialized each turn, but before static fields one-time initialization
            IO.initialize(this.out, this.getDataDirectory(), this::getDataFile);

            log = new Log(Perceptron.class);

            // modules are static, they are initialized once in a battle and reused during each round
            if (modules == null) {
                RobotMode robotMode = new RobotMode(Config.getRobotMode());
                modules = new ArrayList<>();
                modules.add(new NeuralNetworkModule(robotMode.getWeightMatrixSupplier(), getEnergy(), getWidth(), getHeight(),
                        getBattleFieldWidth(), getBattleFieldHeight()));
                if (robotMode.getRoundResultConsumer().isPresent()) {
                    modules.add(new WinPercentageScoreModule(robotMode.getRoundResultConsumer().get(), getEnergy()));
                }
                modules.add(new CriticalFailureModule());

                Color darkBlue = new Color(0, 30, 50);
                Color lightYellow = new Color(220, 220, 200);
                setColors(darkBlue, darkBlue, darkBlue, lightYellow, lightYellow);

                if (getOthers() > 1) {
                    String exceptionMessage = "This robot was designed for 1 to 1 battles, behaviour is unpredictable.";
                    exceptions.add(exceptionMessage);
                    log.error(exceptionMessage);
                }
            }

            dispatchToModules(PerceptronEvents.newRoundStartedEvent(this));

            roundStarted = true;

        } catch (Throwable t) {
            exceptions.add(t.getMessage());
            log.error(t.getMessage());
        }
    }

    public synchronized void disable() {
        setColors(Color.darkGray, Color.darkGray, Color.darkGray, Color.darkGray, Color.darkGray);
        disabled = true;
    }

    @Override
    public synchronized void onStatus(StatusEvent statusEvent) {
        // do not do anything if the robot is not yet initialized in the run() method
        // or if it is disabled because of too many unrecoverable exceptions
        if (roundStarted && !disabled) {
            dispatchToModules(PerceptronEvents.newTurnStartedEvent(statusEvent.getStatus(), getScannedRobotEvents()));
        }
    }

    @Override
    public synchronized void onRoundEnded(RoundEndedEvent event) {
        // do not do anything if the robot is disabled because of too many unrecoverable exceptions
        if (!disabled) {
            dispatchToModules(PerceptronEvents.newRoundEndedEvent(getOthers(), getEnergy()));
        }
    }

    @Override
    public synchronized void onBattleEnded(BattleEndedEvent event) {

        dispatchToModules(PerceptronEvents.newBattleEndedEvent());

        if (exceptions.isEmpty()) {
            log.info("No exceptions occurred during the battle.");
        } else {
            log.error(exceptions.toString());
        }
    }

    private void dispatchToModules(PerceptronEvent event) {
        modules.forEach(
                module -> {
                    try {
                        module.dispatch(event);
                    } catch (Throwable t) {
                        exceptions.add(t.getMessage());
                        log.error(t.getMessage());
                    }
                }
        );
    }
}
