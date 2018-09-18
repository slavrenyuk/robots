package sergey.lavrenyuk;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.DeathEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
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

public class Perceptron extends AdvancedRobot {

    // fields can not be final, since initialization is possible only in run() method
    private static boolean staticInitialized = false;
    private static Supplier<WeightMatrix> weightMatrixSupplier;
    private static RoundResultConsumer roundResultConsumer;

    private boolean instanceInitialized = false;

    // can not be final, see explanation at the beginning of fields declaration
    // using upper case as a style convention
    private double MAX_ENERGY;
    private double BATTLE_FIELD_WIDTH;
    private double BATTLE_FIELD_HEIGHT;
    private double ROBOT_SIZE;

    private NeuralNetwork neuralNetwork;

    // volatile because of paranoia, should be updated by only one thread, but can not be tested
    private volatile double enemyEnergy;
    private volatile Data2D enemyPosition;
    private volatile Data2D enemyMovement;

    @Override
    public void run() {
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

        neuralNetwork = new NeuralNetwork(weightMatrixSupplier.get());

        // enemy data, which is set to initial rather than real values
        enemyEnergy = getEnergy(); // assume it is the same as ours
        enemyPosition = new Data2D(BATTLE_FIELD_WIDTH / 2, BATTLE_FIELD_HEIGHT / 2); // center of the field
        enemyMovement = new Data2D(0.0, 0.0); // no movement

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        Color darkBlue = new Color(0, 30, 50);
        Color lightYellow = new Color(220, 220, 200);
        setColors(darkBlue, darkBlue, darkBlue, lightYellow, lightYellow);

        if (getOthers() > 1) {
            throw new IllegalStateException("This robot was designed for 1 to 1 battles, behaviour is unpredictable.");
        }

        instanceInitialized = true;
    }

    @Override
    public void onWin(WinEvent event) {
        roundResultConsumer.accept(new Score.RoundResult(true, (float) getEnergy()));
    }

    @Override
    public void onDeath(DeathEvent event) {
        roundResultConsumer.accept(new Score.RoundResult(false, (float) -enemyEnergy));
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        roundResultConsumer.close();
    }

    @Override
    public void onStatus(StatusEvent statusEvent) {
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
    }

    private float[] prepareNeuralNetworkInput(RobotStatus myInfo, ScannedRobotEvent enemyInfo) {

        // all values are normalized before passing to the neural network
        double myEnergy = myInfo.getEnergy() / MAX_ENERGY;
        Data2D myPosition = toNormalizedCenterBasedCoordinates(myInfo.getX(), myInfo.getY(),
                BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT);
        Data2D myMovement = toNormalizedMovement(myInfo.getHeadingRadians(), myInfo.getVelocity(), Rules.MAX_VELOCITY);

        // enemyInfo == null means no enemy was scanned
        // previous values will be used if no enemy was scanned
        if (enemyInfo != null) {
            Data2D enemyCoordinates = calculateCoordinates(myInfo.getX(), myInfo.getY(),
                    myInfo.getHeadingRadians() + enemyInfo.getBearingRadians(), enemyInfo.getDistance());

            this.enemyEnergy = enemyInfo.getEnergy();
            this.enemyPosition = toNormalizedCenterBasedCoordinates(enemyCoordinates.getX(), enemyCoordinates.getY(),
                    BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT);
            this.enemyMovement = toNormalizedMovement(enemyInfo.getHeadingRadians(), enemyInfo.getVelocity(), Rules.MAX_VELOCITY);
        }

        float[] result = new float[WeightMatrix.INPUT_NEURONS];
        result[0] = (float) myEnergy;
        result[1] = (float) myPosition.getX();
        result[2] = (float) myPosition.getY();
        result[3] = (float) myMovement.getX();
        result[4] = (float) myMovement.getY();
        result[5] = (float) this.enemyEnergy;
        result[6] = (float) this.enemyPosition.getX();
        result[7] = (float) this.enemyPosition.getY();
        result[8] = (float) this.enemyMovement.getX();
        result[9] = (float) this.enemyMovement.getY();
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
