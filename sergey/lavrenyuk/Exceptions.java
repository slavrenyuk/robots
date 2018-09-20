package sergey.lavrenyuk;

import java.util.ArrayList;
import java.util.List;

public class Exceptions {

    private final int exceptionsThreshold;
    private final List<ExceptionOccurrences> exceptions  = new ArrayList<>();

    public Exceptions(int exceptionsThreshold) {
        this.exceptionsThreshold = exceptionsThreshold;
    }

    public void add(String exceptionMessage) {
        // find if we already faced such exception and increment its occurrences
        for (ExceptionOccurrences exceptionOccurrences : exceptions) {
            if (exceptionOccurrences.exceptionMessage.equals(exceptionMessage)) {
                exceptionOccurrences.occurrences++;
                return;
            }
        }

        // this is a new exception, save it if we didn't reach a limit yet
        if (exceptions.size() < exceptionsThreshold) {
            exceptions.add(new ExceptionOccurrences(exceptionMessage));
        }
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

    private static class ExceptionOccurrences {

        int occurrences = 0;
        final String exceptionMessage;

        ExceptionOccurrences(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }

        @Override
        public String toString() {
            return String.format("occurrences = %d, exception message = %s", occurrences, exceptionMessage);
        }
    }
}
