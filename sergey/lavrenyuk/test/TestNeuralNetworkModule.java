package sergey.lavrenyuk.test;

import robocode.Rules;
import sergey.lavrenyuk.event.EnemyStatus;
import sergey.lavrenyuk.event.PerceptronStatus;
import sergey.lavrenyuk.event.RoundStarted;
import sergey.lavrenyuk.event.TurnStarted;
import sergey.lavrenyuk.module.NeuralNetworkModule;
import sergey.lavrenyuk.module.PerceptronModule;
import sergey.lavrenyuk.nn.WeightMatrixGenerator;
import sergey.lavrenyuk.test.base.PerceptronTestControl;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;

public class TestNeuralNetworkModule {

    public static void main(String[] args) {
        Runner.runTests(TestNeuralNetworkModule.class);
    }

    public void testNeuralNetworkModule() {
        final int ROBOT_ENERGY = 100;
        final int ROBOT_SIZE = 36;
        final int FIELD_WIDTH = 800;
        final int FIELD_HEIGHT = 600;

        PerceptronTestControl control = new PerceptronTestControl();
        PerceptronModule neuralNetworkModule = new NeuralNetworkModule(
                () -> new WeightMatrixGenerator().generateFixed(1), ROBOT_ENERGY, ROBOT_SIZE, ROBOT_SIZE, FIELD_WIDTH, FIELD_HEIGHT);

        assertCondition(!control.isAdjustGunForRobot());
        assertCondition(!control.isAdjustRadarForRobot());

        neuralNetworkModule.dispatch(new RoundStarted(control));
        assertCondition(control.isAdjustGunForRobot());
        assertCondition(control.isAdjustRadarForRobot());

        assertCondition(control.getAheadCommands().isEmpty());
        assertCondition(control.getTurnRightCommands().isEmpty());
        assertCondition(control.getTurnRadarRightCommands().isEmpty());
        assertCondition(control.getTurnGunRightCommands().isEmpty());
        assertCondition(control.getFireCommands().isEmpty());

        // EnemyStatus null means enemy was not scanned
        neuralNetworkModule.dispatch(
                new TurnStarted(new PerceptronStatus(ROBOT_ENERGY, FIELD_WIDTH / 2, FIELD_HEIGHT / 2, 0, 0, 0, 0, 0), null));
        assertCondition(control.getAheadCommands().size() == 1);
        assertCondition(control.getTurnRightCommands().size() == 1);
        assertCondition(control.getTurnRadarRightCommands().size() == 1);
        assertCondition(control.getTurnGunRightCommands().isEmpty());
        assertCondition(control.getFireCommands().isEmpty());

        // combination of Perceptron's body heading, gun heading and enemy's bearing means that fire instruction will be issued
        neuralNetworkModule.dispatch(new TurnStarted(
                new PerceptronStatus(ROBOT_ENERGY, FIELD_WIDTH / 2, FIELD_HEIGHT / 2, 0, Rules.GUN_TURN_RATE_RADIANS / 2, 0, 0, 0),
                new EnemyStatus(ROBOT_ENERGY, 0, 0, 0, 50)));
        assertCondition(control.getAheadCommands().size() == 2);
        assertCondition(control.getTurnRightCommands().size() == 2);
        assertCondition(control.getTurnRadarRightCommands().size() == 2);
        assertCondition(control.getTurnGunRightCommands().size() == 1);
        assertCondition(control.getFireCommands().size() == 1);

        // combination of Perceptron's body heading, gun heading and enemy's bearing means that fire instruction will not be issued
        neuralNetworkModule.dispatch(new TurnStarted(
                new PerceptronStatus(ROBOT_ENERGY, FIELD_WIDTH / 2, FIELD_HEIGHT / 2, 0, 0, 0, 0, 0),
                new EnemyStatus(ROBOT_ENERGY, 0, 0, 0, 50)));
        assertCondition(control.getAheadCommands().size() == 3);
        assertCondition(control.getTurnRightCommands().size() == 3);
        assertCondition(control.getTurnRadarRightCommands().size() == 3);
        assertCondition(control.getTurnGunRightCommands().size() == 2);
        assertCondition(control.getFireCommands().size() == 1);

        // neural network input will return bullet power < 0 and fire instruction will not be issued
        neuralNetworkModule.dispatch(new TurnStarted(
                new PerceptronStatus(ROBOT_ENERGY, 0, 0, 0, 0, 0, 0, 0),
                new EnemyStatus(ROBOT_ENERGY, 0, 0, 0, 50)));
        assertCondition(control.getAheadCommands().size() == 4);
        assertCondition(control.getTurnRightCommands().size() == 4);
        assertCondition(control.getTurnRadarRightCommands().size() == 4);
        assertCondition(control.getTurnGunRightCommands().size() == 3);
        assertCondition(control.getFireCommands().size() == 1);
    }
}
