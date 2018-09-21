package sergey.lavrenyuk;

import java.util.ArrayList;
import java.util.List;

public class Exceptions {

    private final int exceptionsLimit;
    private final List<ExceptionInfo> exceptions  = new ArrayList<>();

    public Exceptions(int exceptionsLimit) {
        this.exceptionsLimit = exceptionsLimit;
    }

    public void add(String exceptionMessage) {
        // find if we already faced such exception and increment its occurrences
        for (ExceptionInfo exception : exceptions) {
            if (exception.message.equals(exceptionMessage)) {
                if (exception.occurrences < Long.MAX_VALUE) {
                    exception.occurrences++;
                }
                return;
            }
        }

        // this is a new exception, save it if we didn't reach a limit yet
        if (exceptions.size() < exceptionsLimit) {
            exceptions.add(new ExceptionInfo(exceptionMessage));
        }
    }

    public boolean isEmpty() {
        return exceptions.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Exceptions:\n");
        exceptions.forEach(exceptionOccurrences ->  {
            stringBuilder.append(exceptionOccurrences.toString());
            stringBuilder.append("\n===========================================================\n");
        });
        return stringBuilder.toString();
    }

    private static class ExceptionInfo {

        long occurrences = 0;
        final String message;

        ExceptionInfo(String exceptionMessage) {
            this.message = exceptionMessage;
        }

        @Override
        public String toString() {
            return String.format("occurrences = %d, exception message = %s", occurrences, message);
        }
    }
}
