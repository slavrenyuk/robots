package sergey.lavrenyuk;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.io.Log;

public class MyRobot extends AdvancedRobot {

    private Log log;

    @Override
    public void run() {
        initialize();
        setNoScannedRobotsBehaviour();
        execute();
    }

    private void initialize() {
        IO.initialize(() -> out, this::getDataFile);
        log = new Log(MyRobot.class);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }

    private void setNoScannedRobotsBehaviour() {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    @Override
    public void onStatus(StatusEvent e) {
        if (getScannedRobotEvents().isEmpty()) {
            setNoScannedRobotsBehaviour();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        log.debug("Scanned %s", e.getName());
        setTurnRadarRightRadians(2.0 * getRadarBearingRadians(e.getBearingRadians()));
    }

    private double getRadarBearingRadians(double bodyBearingRadians) {
        return Utils.normalRelativeAngle(getHeadingRadians() - getRadarHeadingRadians() + bodyBearingRadians);
    }

    private double getGunBearingRadians(double bodyBearingRadians) {
        return Utils.normalRelativeAngle(getHeadingRadians() - getGunHeadingRadians() + bodyBearingRadians);
    }
}
