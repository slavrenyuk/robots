package sergey.lavrenyuk.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing exception occurred during battles. It is required, since there are a lot of logs during battle, especially
 * considering the amount of rounds required for scoring. With this class, we collect exception message during the battle
 * and print them at the end.
 */
public class Exceptions {

    private final int exceptionsLimit;
    private final List<ExceptionInfo> exceptions  = new ArrayList<>();

    public Exceptions(int exceptionsLimit) {
        this.exceptionsLimit = exceptionsLimit;
    }

    public void add(String exceptionMessage) {
        // find if we already faced such exception and increment its occurrences
        for (ExceptionInfo exception : exceptions) {
            if (exceptionMessage.equals(exception.getMessage())) {
                exception.incrementOccurrences();
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
        if (isEmpty()) {
            return "No exceptions";
        }

        StringBuilder stringBuilder = new StringBuilder("Exceptions:\n");
        stringBuilder.append("\n===========================================================\n");
        exceptions.forEach(exceptionOccurrences ->  {
            stringBuilder.append(exceptionOccurrences.toString());
            stringBuilder.append("\n===========================================================\n");
        });
        return stringBuilder.toString();
    }

    private static class ExceptionInfo {

        private long occurrences = 1;
        private final String message;

        private ExceptionInfo(String exceptionMessage) {
            this.message = exceptionMessage;
        }

        public void incrementOccurrences() {
            if (occurrences < Long.MAX_VALUE) {
                occurrences++;
            }
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("occurrences = %d, exception message = %s", occurrences, message);
        }
    }
}
