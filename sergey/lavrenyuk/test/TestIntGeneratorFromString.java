package sergey.lavrenyuk.test;

import sergey.lavrenyuk.nn.IntGeneratorFromString;

import java.util.function.Supplier;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;
import static sergey.lavrenyuk.test.base.TestUtils.assertExceptionThrown;

public class TestIntGeneratorFromString {

    public static void main(String[] args) {
        Runner.runTests(TestIntGeneratorFromString.class);
    }

    public void testIntGeneratorFromEmptyString() {
        assertExceptionThrown(
                () -> new IntGeneratorFromString(""),
                NumberFormatException.class,
                "For input string: \"\"");
    }

    public void testIntGeneratorFromIncorrectString() {
        assertExceptionThrown(
                () -> new IntGeneratorFromString("1, abc, 2"),
                NumberFormatException.class,
                "For input string: \"abc\"");
    }

    public void testIntGeneratorFromString() {
        Supplier<Integer> generator = new IntGeneratorFromString("1, 1, 2");
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(1));
        assertCondition(generator.get().equals(2));
        assertCondition(generator.get().equals(1));
    }
}
