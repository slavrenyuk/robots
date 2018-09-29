package sergey.lavrenyuk.geometry;

/**
 * POJO for storing two dimensional data, e.g. point or vector
 */
public class Data2D {
    private final double x;
    private final double y;

    public Data2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
