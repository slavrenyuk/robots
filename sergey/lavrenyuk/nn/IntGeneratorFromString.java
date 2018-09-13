package sergey.lavrenyuk.nn;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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
