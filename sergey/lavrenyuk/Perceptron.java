package sergey.lavrenyuk;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.RobotStatus;
import robocode.RoundEndedEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import sergey.lavrenyuk.geometry.Data2D;
import sergey.lavrenyuk.io.Config;
import sergey.lavrenyuk.io.IO;
import sergey.lavrenyuk.nn.NeuralNetwork;
import sergey.lavrenyuk.nn.NeuralNetworkMode;
import sergey.lavrenyuk.nn.score.RoundResultConsumer;
import sergey.lavrenyuk.nn.score.Score;
import sergey.lavrenyuk.nn.WeightMatrix;

import java.awt.Color;
import java.util.function.Supplier;

import static sergey.lavrenyuk.geometry.GeometryUtils.calculateCoordinates;
import static sergey.lavrenyuk.geometry.GeometryUtils.toBottomLeftBasedCoordinate;
import static sergey.lavrenyuk.geometry.GeometryUtils.toNormalizedCenterBasedCoordinates;
import static sergey.lavrenyuk.geometry.GeometryUtils.toNormalizedMovement;

// TODO document why everything is wrapped into try blocks
public class Perceptron extends AdvancedRobot {

    // fields can not be final, since initialization is possible only in run() method
    private static boolean staticInitialized = false;
    private static Supplier<WeightMatrix> weightMatrixSupplier;
    private static RoundResultConsumer roundResultConsumer;

    // initially true, will be set to false in the run() method
    // inspect run() for additional details
    private static volatile boolean roundResultReported = true;

    // need to have this flag since we can not fully initialize robot in a constructor,
    // but only in a run() method and first onStatus event is issued before run() method
    private boolean instanceInitialized = false;

    // can not be final, see explanation at the beginning of fields declaration
    // using upper case as a style convention
    private double MAX_ENERGY;
    private double BATTLE_FIELD_WIDTH;
    private double BATTLE_FIELD_HEIGHT;
    private double ROBOT_SIZE;

    // heart and brains of the robot
    private NeuralNetwork neuralNetwork;

    // should be updated by only one thread, but can not be tested, thus volatile
    // this values are normalized, i.e:
    // - normalizedEnemyEnergy is scaled to have values from 0 to 1
    // - normalizedEnemyPosition and normalizedEnemyMovement are scaled to have values from -1 to 1
    // for additional info see prepareNeuralNetworkInput() method
    private volatile double normalizedEnemyEnergy = 1; // full health
    private volatile Data2D normalizedEnemyPosition = new Data2D(0, 0); // center of the battlefield
    private volatile Data2D normalizedEnemyMovement = new Data2D(0, 0); // no movement

    /**
     * This method is called once at the beginning of each round. It is an entry point of the robot's logic.
     * Here we have base robot class fully initialized and set up, not in the class constructor as we get used in the Java world.
     * Keep in mind that first {@link #onStatus(StatusEvent)} happens before this method is called.
     */
    @Override
    public void run() {
        try {

            IO.initialize(() -> out, this::getDataFile);

            if (!staticInitialized) {
                NeuralNetworkMode neuralNetworkMode = new NeuralNetworkMode(Config.getNeuralNetworkMode());
                weightMatrixSupplier = neuralNetworkMode.getWeightMatrixSupplier();
                roundResultConsumer = neuralNetworkMode.getRoundResultConsumer();
                staticInitialized = true;
            }

            MAX_ENERGY = getEnergy();
            BATTLE_FIELD_WIDTH = getBattleFieldWidth();
            BATTLE_FIELD_HEIGHT = getBattleFieldHeight();
            ROBOT_SIZE = getWidth(); // the same as getHeight() and should be equal to 36

            if (!roundResultReported) {
                // round result was not reported in the previous round
                // that may happen because of a nonrecoverable exception
                // at least we should report a dummy score to not break the whole process
                // there is a chance the we will fail during reporting, in that case there is nothing we can do
                // also this will not help if we failed to report during the last round
                roundResultConsumer.accept(new Score.RoundResult(false, (float) -MAX_ENERGY));
            }
            roundResultReported = false;

            neuralNetwork = new NeuralNetwork(weightMatrixSupplier.get());

            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);

            Color darkBlue = new Color(0, 30, 50);
            Color lightYellow = new Color(220, 220, 200);
            setColors(darkBlue, darkBlue, darkBlue, lightYellow, lightYellow);

            if (getOthers() > 1) {
                out.println("This robot was designed for 1 to 1 battles, behaviour is unpredictable.");
            }

            instanceInitialized = true;

        } catch (Throwable t) {
            out.println(t.getMessage());
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        try {
            if (getOthers() == 0) {
                normalizedEnemyEnergy = 0.0;
            }
            roundResultConsumer.accept(new Score.RoundResult(
                    getEnergy() > 0.0 && getOthers() == 0,
                    (float) (getEnergy() - normalizedEnemyEnergy * MAX_ENERGY)
            ));
            roundResultReported = true;
        } catch (Throwable t) {
            out.println(t.getMessage());
        }
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        try {
            roundResultConsumer.close();
        } catch (Throwable t) {
            out.println(t.getMessage());
        }
    }

    @Override
    public void onStatus(StatusEvent statusEvent) {
        try {
            if (!instanceInitialized) {
                return;
            }

            boolean enemyScanned = false;
            double enemyBearingRadians = 0.0;
            ScannedRobotEvent enemyInfo = null;
            if (!getScannedRobotEvents().isEmpty()) {
                // expecting only 1 scanned robot, this robot was designed for 1 to 1 battles
                enemyInfo = getScannedRobotEvents().get(0);
                enemyScanned = true;
                enemyBearingRadians = enemyInfo.getBearingRadians();
            }

            float[] input = prepareNeuralNetworkInput(statusEvent.getStatus(), enemyInfo);
            float[] output = neuralNetwork.process(input);
            issueInstructions(enemyScanned, enemyBearingRadians, output);
        } catch (Throwable t) {
            out.println(t.getMessage());
        }
    }

    private float[] prepareNeuralNetworkInput(RobotStatus myInfo, ScannedRobotEvent enemyInfo) {

        // all values are normalized before passing to the neural network
        double myNormalizedEnergy = myInfo.getEnergy() / MAX_ENERGY;
        Data2D myNormalizedPosition = toNormalizedCenterBasedCoordinates(myInfo.getX(), myInfo.getY(),
                BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT);
        Data2D myNormalizedMovement = toNormalizedMovement(myInfo.getHeadingRadians(), myInfo.getVelocity(), Rules.MAX_VELOCITY);

        // enemyInfo == null means no enemy was scanned
        // previous values will be used if no enemy was scanned
        if (enemyInfo != null) {
            Data2D enemyCoordinates = calculateCoordinates(myInfo.getX(), myInfo.getY(),
                    myInfo.getHeadingRadians() + enemyInfo.getBearingRadians(), enemyInfo.getDistance());

            this.normalizedEnemyEnergy = enemyInfo.getEnergy() / MAX_ENERGY;
            this.normalizedEnemyPosition = toNormalizedCenterBasedCoordinates(enemyCoordinates.getX(), enemyCoordinates.getY(),
                    BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT);
            this.normalizedEnemyMovement = toNormalizedMovement(enemyInfo.getHeadingRadians(), enemyInfo.getVelocity(), Rules.MAX_VELOCITY);
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

    private void issueInstructions(boolean enemyScanned, double enemyBearingRadians, float[] neuralNetworkOutput) {

        // 1st and 2nd elements are treated as normalized center based coordinates we should move to
        double gotoX = toBottomLeftBasedCoordinate(neuralNetworkOutput[0], BATTLE_FIELD_WIDTH, ROBOT_SIZE);
        double gotoY = toBottomLeftBasedCoordinate(neuralNetworkOutput[1], BATTLE_FIELD_HEIGHT, ROBOT_SIZE);

        // 3rd element is treated as a bullet power, negative values means do not fire
        double bulletPower = neuralNetworkOutput[2] * Rules.MAX_BULLET_POWER;
        // 4th element is treated as the gun correction relative to direct targeting on enemy
        // this makes sense since it takes some time for a bullet to reach the enemy
        // correction range is [-45, 45] degrees
        double gunCorrectionRadians = neuralNetworkOutput[3] * Math.PI / 4;

        issueRadarInstructions(enemyScanned, enemyBearingRadians);
        issueGunInstructions(enemyScanned, bulletPower, enemyBearingRadians, gunCorrectionRadians);
        issueBodyInstructions(gotoX, gotoY);
    }

    private void issueRadarInstructions(boolean enemyScanned, double enemyBearingRadians) {
        // multiply by 2, so enemy will be in the middle of radar arc
        // if not scanned turn infinitely (another instruction will be issued when scanned)
        setTurnRadarRightRadians(enemyScanned ? 2.0 * getRadarBearingRadians(enemyBearingRadians) : Double.POSITIVE_INFINITY);
    }

    private double getRadarBearingRadians(double bodyBearingRadians) {
        return Utils.normalRelativeAngle(getHeadingRadians() - getRadarHeadingRadians() + bodyBearingRadians);
    }

    private void issueGunInstructions(boolean enemyScanned, double bulletPower,
                                      double enemyBearingRadians, double correctionRadians) {
        if (enemyScanned) {
            double gunBearingRadians = getGunBearingRadians(enemyBearingRadians + correctionRadians);
            setTurnGunRightRadians(gunBearingRadians);
            if (bulletPower > 0.0 && gunBearingRadians < Rules.GUN_TURN_RATE_RADIANS) {
                setFire(bulletPower);
            }
        }
    }

    private double getGunBearingRadians(double bodyBearingRadians) {
        return Utils.normalRelativeAngle(getHeadingRadians() - getGunHeadingRadians() + bodyBearingRadians);
    }

    private void issueBodyInstructions(double gotoX, double gotoY) {
        double xDiff = gotoX - getX();
        double yDiff = gotoY - getY();
        double angle = Utils.normalRelativeAngle(Math.atan2(xDiff, yDiff) - getHeadingRadians());

        // trigonometry that makes robot turn less in case it may go back instead of ahead
        setTurnRightRadians(Math.atan(Math.tan(angle)));

        // cos makes the value negative in case we need to move back
        // also it reduces the amount moved more the more perpendicular it is to the desired angle of travel
        setAhead(Math.cos(angle) * Math.hypot(xDiff, yDiff));
    }
}
