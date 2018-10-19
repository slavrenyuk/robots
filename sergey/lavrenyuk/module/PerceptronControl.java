package sergey.lavrenyuk.module;

public interface PerceptronControl {

    void setAhead(double distance);

    void setFire(double power);

    void setTurnRightRadians(double radians);

    void setTurnGunRightRadians(double radians);

    void setTurnRadarRightRadians(double radians);

    void setAdjustGunForRobotTurn(boolean independent);

    void setAdjustRadarForGunTurn(boolean independent);

    void disable();
}
