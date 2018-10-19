package sergey.lavrenyuk.event;

public class EnemyStatus {

    private final double energy;
    private final double velocity;
    private final double headingRadians;
    private final double bearingRadians;
    private final double distance;

    public EnemyStatus(double energy, double velocity, double headingRadians, double bearingRadians, double distance) {
        this.energy = energy;
        this.velocity = velocity;
        this.headingRadians = headingRadians;
        this.bearingRadians = bearingRadians;
        this.distance = distance;
    }

    public double getEnergy() {
        return energy;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public double getBearingRadians() {
        return bearingRadians;
    }

    public double getDistance() {
        return distance;
    }
}
