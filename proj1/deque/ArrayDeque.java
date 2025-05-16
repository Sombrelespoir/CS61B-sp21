package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    private static final int INITIAL_CAPACITY = 8;
    private static final double MIN_USAGE_RATIO = 0.25;

    public ArrayDeque() {
        items = (T[]) new Object[INITIAL_CAPACITY];
        size = 0;
        nextFirst = 4;
        nextLast = 5;
    }

    private void resize(int capacity) {
        T[] newArray = (T[]) new Object[capacity];
        int cur = plusOne(nextFirst);
        for (int i = 0; i < size; i++) {
            newArray[i] = items[cur];
            cur = plusOne(cur);
        }
        items = newArray;
        nextFirst = capacity - 1;
        nextLast = size;
    }

    private int minusOne(int index) {
        return (index - 1 + items.length) % items.length;
    }

    private int plusOne(int index) {
        return (index + 1) % items.length;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = minusOne(nextFirst);
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = plusOne(nextLast);
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        int cur = plusOne(nextFirst);
        for (int i = 0; i < size; i++) {
            System.out.print(items[cur]);
            System.out.print(" ");
            cur = plusOne(cur);
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = plusOne(nextFirst);
        T removed = items[nextFirst];
        items[nextFirst] = null;
        size--;

        if (size > 0 && size < items.length * MIN_USAGE_RATIO) {
            resize(items.length / 2);
        }
        return removed;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = minusOne(nextLast);
        T removed = items[nextLast];
        items[nextLast] = null;
        size--;

        if (size > 0 && size < items.length * MIN_USAGE_RATIO) {
            resize(items.length / 2);
        }
        return removed;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        int realIndex = (plusOne(nextFirst) + index) % items.length;
        return items[realIndex];
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int position;
        private int count;

        ArrayDequeIterator() {
            position = plusOne(nextFirst);
            count = 0;
        }

        public boolean hasNext() {
            return count < size;
        }

        public T next() {
            T item = items[position];
            position = plusOne(position);
            count++;
            return item;
        }
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<?> other = (Deque<?>) o;
        if (size != other.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }
}


















