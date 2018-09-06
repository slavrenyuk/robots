package sergey.lavrenyuk.geometry;

public class GeometryUtils {

    private GeometryUtils() {}

    public static Data2D calculateCoordinates(double startX, double startY, double angleRadians, double distance) {
        return new Data2D(
                startX + Math.sin(angleRadians) * distance,
                startY + Math.cos(angleRadians) * distance);
    }

    public static Data2D toNormalizedCenterBasedCoordinates(double bottomLeftBasedX, double bottomLeftBasedY,
                                                            double width, double height) {
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        return new Data2D(
                (bottomLeftBasedX - halfWidth) / halfWidth,
                (bottomLeftBasedY - halfHeight) / halfHeight);
    }

    public static double toBottomLeftBasedCoordinate(double normalizedCenterBasedCoord, double axisLength, double borderMargin) {
        double halfAxisLength = axisLength / 2;
        double result = normalizedCenterBasedCoord * halfAxisLength + halfAxisLength;
        return result < borderMargin
                ? borderMargin
                : result > axisLength - borderMargin
                ? axisLength - borderMargin
                : result;
    }

    public static Data2D toNormalizedMovement(double headingRadians, double velocity, double maxVelocity) {
        // normal trigonometry is counterclockwise (the angle increases to the left)
        // but Robocode trigonometry is clockwise, thus sin and cos functions and replaced with each other
        double normalizedVelocity = velocity / maxVelocity;
        return new Data2D(
                Math.sin(headingRadians) * normalizedVelocity,
                Math.cos(headingRadians) * normalizedVelocity
        );
    }
}
