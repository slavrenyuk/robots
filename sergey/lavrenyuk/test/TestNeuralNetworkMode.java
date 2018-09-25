package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.NeuralNetworkMode;

import java.io.IOException;

import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.fail;

public class TestNeuralNetworkMode {

    public static void main(String[] args) {
        Runner.runTests(TestNeuralNetworkMode.class);
    }

    public void testIncorectMode() {
        assertExceptionThrown(
                () -> {
                    try {
                        new NeuralNetworkMode("INCORRECT");
                    } catch (IOException ex) {
                        fail(ex.getMessage());
                    }
                },
                IllegalArgumentException.class,
                "Unsupported neural network mode INCORRECT");
    }
}
