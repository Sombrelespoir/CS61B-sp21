package deque;

public interface Deque<T> {

    void addFirst(T item);

    void addLast(T item);

    int size();

    T removeFirst();

    T removeLast();

    T get(int index);

    void printDeque();

    default boolean isEmpty() {
        return size() == 0;
    }

}

