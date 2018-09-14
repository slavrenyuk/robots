package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.NeuralNetworkMode;

import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;

public class TestNeuralNetworkMode {

    public static void main(String[] args) {
        Runner.runTests(TestNeuralNetworkMode.class);
    }

    public void testIncorectMode() {
        assertExceptionThrown(
                () -> new NeuralNetworkMode("INCORRECT"),
                IllegalArgumentException.class,
                "Unsupported neural network mode INCORRECT");
    }
}
