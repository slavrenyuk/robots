package sergey.lavrenyuk.nn;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Generator that consumes a string parameter in a constructor and supplies integer values via {@link #get()}.
 * Format of the provided string is a list of integer numbers separate by comma (optionally with whitespaces), e.g.: "1, 1, 2, 3".
 * If string "1, 1, 2, 3" was provided, {@link #get()} first invocation will return 1, the second will return 1, then 2, then 3,
 * then again 1, 1, 2, 3, and so on.
 */
public class IntGeneratorFromString implements Supplier<Integer> {

    private final Integer[] intArray;
    private final AtomicInteger index;

    public IntGeneratorFromString(String str) {
        this.intArray = Arrays
                .stream(str.split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .toArray(Integer[]::new);
        this.index = new AtomicInteger(0);
    }

    @Override
    public Integer get() {
        return intArray[index.getAndUpdate(i -> ++i < intArray.length ? i : 0)];
    }
}
