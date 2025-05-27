package hashmap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author ZhangYusen
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private int items_num;
    private double maxLoadFactor;

    /** Constructors */
    public MyHashMap() {
        buckets = null;
        size = 0;
        items_num = 0;
        maxLoadFactor = 0.0;
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
        size = initialSize;
        items_num = 0;
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        maxLoadFactor = 0.0;
    }
    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this(initialSize);
        this.maxLoadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new java.util.LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public void clear() {
        items_num = 0;
        size = 0;
        buckets = null;
    }

    @Override
    public boolean containsKey(K key) {
        if (buckets == null) return false;

        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[bucketIndex];

        if (bucket == null) return false;

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        if (buckets == null) return null;

        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[bucketIndex];

        if (bucket == null) return null;

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return items_num;
    }

    @Override
    public void put(K key, V value) {
        if (buckets == null) {
            buckets = createTable(16);
            size = 16;
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = createBucket();
            }
        }

        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[bucketIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }

        bucket.add(createNode(key, value));
        items_num++;

        if (maxLoadFactor > 0 && (double) items_num / size > maxLoadFactor) {
            resize();
        }
    }

    private void resize() {
        Collection<Node>[] oldBuckets = buckets;
        int oldSize = size;

        size = size * 2;
        buckets = createTable(size);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        items_num = 0;

        for (int i = 0; i < oldSize; i++) {
            if (oldBuckets[i] != null) {
                for (Node node : oldBuckets[i]) {
                    put(node.key, node.value);
                }
            }
        }
    }


    @Override
    public Set<K> keySet() {
        Set<K> keys = new java.util.HashSet<>();
        if (buckets != null) {
            for (Collection<Node> bucket : buckets) {
                if (bucket != null) {
                    for (Node node : bucket) {
                        keys.add(node.key);
                    }
                }
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        if (buckets == null) return null;

        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[bucketIndex];

        if (bucket == null) return null;

        Iterator<Node> iter = bucket.iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node.key.equals(key)) {
                iter.remove();
                items_num--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        if (buckets == null) return null;

        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        Collection<Node> bucket = buckets[bucketIndex];

        if (bucket == null) return null;

        Iterator<Node> iter = bucket.iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node.key.equals(key) && node.value.equals(value)) {
                iter.remove();
                items_num--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {
        private int bucketIndex = 0;
        private Iterator<Node> currentBucketIterator;

        public MyHashMapIterator() {
            findNextNonEmptyBucket();
        }

        private void findNextNonEmptyBucket() {
            currentBucketIterator = null;
            if (buckets != null) {
                while (bucketIndex < buckets.length) {
                    if (buckets[bucketIndex] != null && !buckets[bucketIndex].isEmpty()) {
                        currentBucketIterator = buckets[bucketIndex].iterator();
                        break;
                    }
                    bucketIndex++;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return currentBucketIterator != null && currentBucketIterator.hasNext();
        }

        @Override
        public K next() {
            if (!hasNext()) {
            throw new java.util.NoSuchElementException();
            }
        
            K key = currentBucketIterator.next().key;
        
            if (!currentBucketIterator.hasNext()) {
                bucketIndex++;
                findNextNonEmptyBucket();
            }
        
            return key;
        }
    }

}


