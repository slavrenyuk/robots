package sergey.lavrenyuk.event;

public class PerceptronStatus {

    private final double energy;
    private final double x;
    private final double y;
    private final double bodyHeadingRadians;
    private final double gunHeadingRadians;
    private final double radarHeadingRadians;
    private final double velocity;
    private final double gunHeat;

    public PerceptronStatus(double energy, double x, double y, double bodyHeadingRadians, double gunHeadingRadians,
                            double radarHeadingRadians, double velocity, double gunHeat) {
        this.energy = energy;
        this.x = x;
        this.y = y;
        this.bodyHeadingRadians = bodyHeadingRadians;
        this.gunHeadingRadians = gunHeadingRadians;
        this.radarHeadingRadians = radarHeadingRadians;
        this.velocity = velocity;
        this.gunHeat = gunHeat;
    }

    public double getEnergy() {
        return energy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getBodyHeadingRadians() {
        return bodyHeadingRadians;
    }

    public double getGunHeadingRadians() {
        return gunHeadingRadians;
    }

    public double getRadarHeadingRadians() {
        return radarHeadingRadians;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getGunHeat() {
        return gunHeat;
    }
}
