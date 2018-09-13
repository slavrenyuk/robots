package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.NeuralNetworkMode;
import sergey.lavrenyuk.test.base.BaseTest;

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
}
