package sergey.lavrenyuk.test;

import sergey.lavrenyuk.io.MaxValuesStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestMaxValuesStorage extends Test {

    public static void main(String[] args) {
        new TestMaxValuesStorage().run();
    }

    public void testNotFullStorage() {
        Comparator<Integer> intComparator = Comparator.naturalOrder();
        MaxValuesStorage<Integer> storage = new MaxValuesStorage<>(5, intComparator);
        storage.put(1);
        storage.put(2);

        List<Integer> actualList = storage.asList();
        List<Integer> expectedList = new ArrayList<>();
        expectedList.add(2);
        expectedList.add(1);

        assertCondition(actualList.equals(expectedList));
    }

    public void testFullPath() {
        Comparator<Integer> intComparator = Comparator.naturalOrder();
        MaxValuesStorage<Integer> storage = new MaxValuesStorage<>(3, intComparator);
        storage.put(0);
        storage.put(2);
        storage.put(-1);
        storage.put(4);
        storage.put(-6);

        List<Integer> actualList = storage.asList();
        List<Integer> expectedList = new ArrayList<>();
        expectedList.add(4);
        expectedList.add(2);
        expectedList.add(0);
        assertCondition(actualList.equals(expectedList));
    }
}
