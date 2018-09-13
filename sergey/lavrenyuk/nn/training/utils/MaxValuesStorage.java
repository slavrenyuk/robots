package sergey.lavrenyuk.nn.training.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxValuesStorage<T> {

    private final Object[] array; // can not use generics here because it is an array
    private final Comparator<T> comparator;

    private int size = 0;
    private int minValueIndex = 0;

    public MaxValuesStorage(int sizeLimit, Comparator<T> comparator) {
        this.array = new Object[sizeLimit];
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    public void put(T value) {
        if (size < array.length) {
            array[size] = value;
            if (comparator.compare(value, (T) array[minValueIndex]) < 0) {
                minValueIndex = size;
            }
            size++;
            return;
        }
        if (comparator.compare(value, (T) array[minValueIndex]) > 0) {
            array[minValueIndex] = value;
            for (int i = 0; i < array.length; i++) {
                if (comparator.compare((T) array[i], (T) array[minValueIndex]) < 0) {
                    minValueIndex = i;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> asList() {
        List<T> result = (List<T>) Arrays.asList(Arrays.copyOf(array, size));
        result.sort(Collections.reverseOrder(comparator)); // max elements go first
        return result;
    }
}
