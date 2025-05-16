package deque;

import org.checkerframework.checker.units.qual.C;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> defaultComparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        defaultComparator = c;
    }

    public T max() {
        return max(defaultComparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T curItem = get(i);
            if (c.compare(curItem, maxItem) > 0) {
                maxItem = curItem;
            }
        }
        return maxItem;
    }
}

