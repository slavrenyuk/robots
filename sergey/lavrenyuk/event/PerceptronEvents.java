package sergey.lavrenyuk.event;

import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import sergey.lavrenyuk.module.PerceptronControl;

import java.util.List;

public class PerceptronEvents {

    private PerceptronEvents() {}

    public static RoundStarted newRoundStartedEvent(PerceptronControl robotControl) {
        return new RoundStarted(robotControl);
    }

    public static TurnStarted newTurnStartedEvent(RobotStatus robotStatus, List<ScannedRobotEvent> scannedRobotEvents) {
        return new TurnStarted(getPerceptronStatus(robotStatus), getEnemyStatus(scannedRobotEvents));
    }

    public static RoundEnded newRoundEndedEvent(int enemies, double perceptronEnergy) {
        return new RoundEnded(enemies == 0 && perceptronEnergy > 0);
    }

    public static BattleEnded newBattleEndedEvent() {
        return new BattleEnded();
    }

    private static PerceptronStatus getPerceptronStatus(RobotStatus status) {
        return new PerceptronStatus(
                status.getEnergy(),
                status.getX(),
                status.getY(),
                status.getHeadingRadians(),
                status.getGunHeadingRadians(),
                status.getRadarHeadingRadians(),
                status.getVelocity(),
                status.getGunHeat()
        );
    }

    private static EnemyStatus getEnemyStatus(List<ScannedRobotEvent> scannedRobotEvents) {
        if (scannedRobotEvents.isEmpty()) {
            return null;
        }
        // expecting only 1 scanned robot, this robot was designed for 1 to 1 battles
        ScannedRobotEvent scannedRobotEvent = scannedRobotEvents.get(0);
        return new EnemyStatus(
                scannedRobotEvent.getEnergy(),
                scannedRobotEvent.getVelocity(),
                scannedRobotEvent.getHeadingRadians(),
                scannedRobotEvent.getBearingRadians(),
                scannedRobotEvent.getDistance());
    }
}
