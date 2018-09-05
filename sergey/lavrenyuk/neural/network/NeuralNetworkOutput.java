package sergey.lavrenyuk.neural.network;

public class NeuralNetworkOutput {

    private final double firePower;
    private final double gunShift;
    private final double newX;
    private final double newY;

    public NeuralNetworkOutput(double firePower, double gunShift, double newX, double newY) {
        this.firePower = firePower;
        this.gunShift = gunShift;
        this.newX = newX;
        this.newY = newY;
    }

    public double getFirePower() {
        return firePower;
    }

    public double getGunShift() {
        return gunShift;
    }

    public double getNewX() {
        return newX;
    }

    public double getNewY() {
        return newY;
    }
}
