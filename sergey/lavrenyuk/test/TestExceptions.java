package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.Exceptions;

import static sergey.lavrenyuk.test.base.TestUtils.assertCondition;

public class TestExceptions {

    public static void main(String[] args) {
        Runner.runTests(TestExceptions.class);
    }

    public void test() {
        Exceptions exceptions = new Exceptions(2);
        assertCondition(exceptions.isEmpty());
        assertCondition(exceptions.toString().contains("No exceptions"));

        exceptions.add("first exception");
        assertCondition(exceptions.toString().contains("occurrences = 1, exception message = first exception"));

        exceptions.add("second exception");
        exceptions.add("second exception");
        assertCondition(exceptions.toString().contains("occurrences = 2, exception message = second exception"));

        exceptions.add("third exception"); // limit is 2 different exceptions
        assertCondition(!exceptions.toString().contains("exception message = third exception"));
    }
}
