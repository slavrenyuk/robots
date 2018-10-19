package sergey.lavrenyuk.module;

import robocode.Rules;
import robocode.util.Utils;
import sergey.lavrenyuk.event.EnemyStatus;
import sergey.lavrenyuk.event.PerceptronEvent;
import sergey.lavrenyuk.event.PerceptronStatus;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.event.TurnStarted;
import sergey.lavrenyuk.geometry.Data2D;
import sergey.lavrenyuk.nn.NeuralNetwork;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.util.function.Supplier;

import static sergey.lavrenyuk.geometry.GeometryUtils.calculateCoordinates;
import static sergey.lavrenyuk.geometry.GeometryUtils.toBottomLeftBasedCoordinate;
import static sergey.lavrenyuk.geometry.GeometryUtils.toNormalizedCenterBasedCoordinates;
import static sergey.lavrenyuk.geometry.GeometryUtils.toNormalizedMovement;

public class NeuralNetworkModule implements PerceptronModule {

    private final Supplier<WeightMatrix> weightMatrixSupplier;

    private final double robotMaxEnergy;
    private final double robotWidth;
    private final double robotHeight;
    private final double battleFieldWidth;
    private final double battleFieldHeight;

    // robot control
    private volatile PerceptronControl robotControl;

    // heart and brains of the robot
    private volatile NeuralNetwork neuralNetwork;

    // should be updated by only one thread, but can not be tested, thus volatile
    // this values are normalized, i.e:
    // - normalizedEnemyEnergy is scaled to have values from 0 to 1
    // - normalizedEnemyPosition and normalizedEnemyMovement are scaled to have values from -1 to 1
    // for additional info see prepareNeuralNetworkInput() method
    private volatile double normalizedEnemyEnergy;
    private volatile Data2D normalizedEnemyPosition;
    private volatile Data2D normalizedEnemyMovement;

    public NeuralNetworkModule(Supplier<WeightMatrix> weightMatrixSupplier, double robotMaxEnergy,
                               double robotWidth, double robotHeight, double battleFieldWidth, double battleFieldHeight) {
        this.weightMatrixSupplier = weightMatrixSupplier;
        this.robotMaxEnergy = robotMaxEnergy;
        this.robotWidth = robotWidth;
        this.robotHeight = robotHeight;
        this.battleFieldWidth = battleFieldWidth;
        this.battleFieldHeight = battleFieldHeight;
    }

    @Override
    public void dispatch(PerceptronEvent event) {
        if (event instanceof RoundStarted) {
            onRoundStarted((RoundStarted) event);
        } else if (event instanceof TurnStarted) {
            onTurnStarted((TurnStarted) event);
        }
    }

    public void onRoundStarted(RoundStarted event) {
        robotControl = event.getRobotControl();
        neuralNetwork = new NeuralNetwork(weightMatrixSupplier.get());

        robotControl.setAdjustGunForRobotTurn(true);
        robotControl.setAdjustRadarForGunTurn(true);

        normalizedEnemyEnergy = 1; // full health
        normalizedEnemyPosition = new Data2D(0, 0); // center of the battlefield
        normalizedEnemyMovement = new Data2D(0, 0); // no movement
    }

    public void onTurnStarted(TurnStarted event) {
        PerceptronStatus perceptronStatus = event.getPerceptronStatus();
        // enemyStatus == null means no enemy was scanned this turn
        EnemyStatus enemyStatus = event.getEnemyStatus().orElse(null);

        float[] input = prepareNeuralNetworkInput(perceptronStatus, enemyStatus);
        float[] output = neuralNetwork.process(input);
        issueInstructions(perceptronStatus, enemyStatus, output);
    }

    private float[] prepareNeuralNetworkInput(PerceptronStatus perceptronStatus, EnemyStatus enemyStatus) {

        // all values are normalized before passing to the neural network
        double myNormalizedEnergy = perceptronStatus.getEnergy() / robotMaxEnergy;
        Data2D myNormalizedPosition = toNormalizedCenterBasedCoordinates(perceptronStatus.getX(), perceptronStatus.getY(),
                battleFieldWidth, battleFieldHeight);
        Data2D myNormalizedMovement =
                toNormalizedMovement(perceptronStatus.getBodyHeadingRadians(), perceptronStatus.getVelocity(), Rules.MAX_VELOCITY);

        // enemyStatus == null means no enemy was scanned
        // previous values will be used if no enemy was scanned
        if (enemyStatus != null) {
            Data2D enemyCoordinates = calculateCoordinates(perceptronStatus.getX(), perceptronStatus.getY(),
                    perceptronStatus.getBodyHeadingRadians() + enemyStatus.getBearingRadians(), enemyStatus.getDistance());

            this.normalizedEnemyEnergy = enemyStatus.getEnergy() / robotMaxEnergy;
            this.normalizedEnemyPosition = toNormalizedCenterBasedCoordinates(enemyCoordinates.getX(), enemyCoordinates.getY(),
                    battleFieldWidth, battleFieldHeight);
            this.normalizedEnemyMovement = toNormalizedMovement(enemyStatus.getHeadingRadians(), enemyStatus.getVelocity(), Rules.MAX_VELOCITY);
        }

        float[] result = new float[WeightMatrix.INPUT_NEURONS];
        result[0] = (float) myNormalizedEnergy;
        result[1] = (float) myNormalizedPosition.getX();
        result[2] = (float) myNormalizedPosition.getY();
        result[3] = (float) myNormalizedMovement.getX();
        result[4] = (float) myNormalizedMovement.getY();
        result[5] = (float) this.normalizedEnemyEnergy;
        result[6] = (float) this.normalizedEnemyPosition.getX();
        result[7] = (float) this.normalizedEnemyPosition.getY();
        result[8] = (float) this.normalizedEnemyMovement.getX();
        result[9] = (float) this.normalizedEnemyMovement.getY();
        return result;
    }

    private void issueInstructions(PerceptronStatus perceptronStatus, EnemyStatus enemyStatus, float[] neuralNetworkOutput) {

        // 1st and 2nd elements are treated as normalized center based coordinates we should move to
        double gotoX = toBottomLeftBasedCoordinate(neuralNetworkOutput[0], battleFieldWidth, robotWidth);
        double gotoY = toBottomLeftBasedCoordinate(neuralNetworkOutput[1], battleFieldHeight, robotHeight);

        // 3rd element is treated as a bullet power, negative values means do not fire
        double bulletPower = neuralNetworkOutput[2] * Rules.MAX_BULLET_POWER;
        // 4th element is treated as the gun correction relative to direct targeting on the enemy
        // this makes sense since it takes some time for a bullet to reach the enemy
        // correction range is taken from a constant Rules.GUN_TURN_RATE_RADIANS and is [-20, 20] degrees
        double gunCorrectionRadians = neuralNetworkOutput[3] *  Rules.GUN_TURN_RATE_RADIANS;

        issueRadarInstructions(perceptronStatus, enemyStatus);
        issueGunInstructions(perceptronStatus, enemyStatus, bulletPower, gunCorrectionRadians);
        issueBodyInstructions(perceptronStatus, gotoX, gotoY);
    }

    private void issueRadarInstructions(PerceptronStatus perceptronStatus, EnemyStatus enemyStatus) {
        // enemyStatus == null means no enemy was scanned
        // if not scanned turn infinitely until another instruction will be issued next turn
        if (enemyStatus == null) {
            robotControl.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            return;
        }

        // multiply by 2, so enemy will be in the middle of radar arc
        double radarBearingRadians = Utils.normalRelativeAngle(
                perceptronStatus.getBodyHeadingRadians() - perceptronStatus.getRadarHeadingRadians() + enemyStatus.getBearingRadians());
        robotControl.setTurnRadarRightRadians(2.0 * radarBearingRadians);
    }

    private void issueGunInstructions(PerceptronStatus perceptronStatus, EnemyStatus enemyStatus, double bulletPower, double gunCorrectionRadians) {
        // enemy was not scanned
        if (enemyStatus == null) {
            return;
        }
        double gunBearingRadians = Utils.normalRelativeAngle(perceptronStatus.getBodyHeadingRadians()
                - perceptronStatus.getGunHeadingRadians() + enemyStatus.getBearingRadians() + gunCorrectionRadians);
        robotControl.setTurnGunRightRadians(gunBearingRadians);
        if (bulletPower > 0.0 && gunBearingRadians < Rules.GUN_TURN_RATE_RADIANS) {
            robotControl.setFire(bulletPower);
        }
    }

    private void issueBodyInstructions(PerceptronStatus perceptronStatus, double gotoX, double gotoY) {
        double xDiff = gotoX - perceptronStatus.getX();
        double yDiff = gotoY - perceptronStatus.getY();
        double angle = Utils.normalRelativeAngle(Math.atan2(xDiff, yDiff) - perceptronStatus.getBodyHeadingRadians());

        // trigonometry that makes robot turn less in case it may go back instead of ahead
        robotControl.setTurnRightRadians(Math.atan(Math.tan(angle)));

        // cos makes the value negative in case we need to move back
        // also it reduces the amount moved more the more perpendicular it is to the desired angle of travel
        robotControl.setAhead(Math.cos(angle) * Math.hypot(xDiff, yDiff));
    }
}
