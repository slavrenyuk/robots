package sergey.lavrenyuk.test.base;

import sergey.lavrenyuk.module.PerceptronControl;

import java.util.ArrayList;
import java.util.List;

public class PerceptronTestControl implements PerceptronControl {

    private List<Double> aheadCommands = new ArrayList<>();
    private List<Double> fireCommands = new ArrayList<>();
    private List<Double> turnRightCommands = new ArrayList<>();
    private List<Double> turnGunRightCommands = new ArrayList<>();
    private List<Double> turnRadarRightCommands = new ArrayList<>();
    private boolean adjustGunForRobot = false;
    private boolean adjustRadarForRobot = false;
    private static boolean disabled = false;

    @Override
    public void setAhead(double distance) {
        aheadCommands.add(distance);
    }

    @Override
    public void setFire(double power) {
        fireCommands.add(power);
    }

    @Override
    public void setTurnRightRadians(double radians) {
        turnRightCommands.add(radians);
    }

    @Override
    public void setTurnGunRightRadians(double radians) {
        turnGunRightCommands.add(radians);
    }

    @Override
    public void setTurnRadarRightRadians(double radians) {
        turnRadarRightCommands.add(radians);
    }

    @Override
    public void setAdjustGunForRobotTurn(boolean independent) {
        adjustGunForRobot = independent;
    }

    @Override
    public void setAdjustRadarForGunTurn(boolean independent) {
        adjustRadarForRobot = independent;
    }

    @Override
    public void disable() {
        disabled = true;
    }

    public List<Double> getAheadCommands() {
        return aheadCommands;
    }

    public List<Double> getFireCommands() {
        return fireCommands;
    }

    public List<Double> getTurnRightCommands() {
        return turnRightCommands;
    }

    public List<Double> getTurnGunRightCommands() {
        return turnGunRightCommands;
    }

    public List<Double> getTurnRadarRightCommands() {
        return turnRadarRightCommands;
    }

    public boolean isAdjustGunForRobot() {
        return adjustGunForRobot;
    }

    public boolean isAdjustRadarForRobot() {
        return adjustRadarForRobot;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
