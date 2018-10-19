package sergey.lavrenyuk.test;

import sergey.lavrenyuk.RobotMode;

import java.io.IOException;

import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;
import static sergey.lavrenyuk.test.base.TestUtils.fail;

public class TestRobotMode {

    public static void main(String[] args) {
        Runner.runTests(TestRobotMode.class);
    }

    public void testIncorectMode() {
        assertExceptionThrown(
                () -> {
                    try {
                        new RobotMode("INCORRECT");
                    } catch (IOException ex) {
                        fail(ex.getMessage());
                    }
                },
                IllegalArgumentException.class,
                "Unsupported robot mode INCORRECT");
    }
}
