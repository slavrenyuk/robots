package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.NeuralNetworkMode;
import sergey.lavrenyuk.test.base.BaseTest;

import java.util.function.Supplier;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;

public class TestNeuralNetworkMode extends BaseTest {

    public static void main(String[] args) {
        new TestNeuralNetworkMode().runTests();
    }

    public void testIncorectMode() {
        assertExceptionThrown(
                () -> new NeuralNetworkMode("INCORRECT"),
                IllegalArgumentException.class,
                "Unsupported neural network mode INCORRECT");
    }

    public void testIntGeneratorFromEmptyString() {
        assertExceptionThrown(
                () -> NeuralNetworkMode.createIntGeneratorFromString(""),
                NumberFormatException.class,
                "For input string: \"\"");
    }

    public void testIntGeneratorFromIncorrectString() {
        assertExceptionThrown(
                () -> NeuralNetworkMode.createIntGeneratorFromString("1, abc, 2"),
                NumberFormatException.class,
                "For input string: \"abc\"");
    }

    public void testIntGeneratorFromString() {
        Supplier<Integer> generator = NeuralNetworkMode.createIntGeneratorFromString("1, 1, 2");
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(2));
        assertCondition(generator.get().equals(1));
    }
}
