package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;
    private int size;

    private class Node {
        private K key;
        private V value;
        private Node left;
        private Node right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(Node node, K key) {
        if (node == null || key == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(node.left, key);
        } else if (cmp > 0) {
            return containsKey(node.right, key);
        } else {
            return true;
        }
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(Node node, K key) {
        if (node == null || key == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key can not be null");
        }
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value) {
        if (node == null) {
            size += 1;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.println(node.key + "->" + node.value);
        printInOrder(node.right);

    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        collectKeys(root, keys);
        return keys;
    }

    private void collectKeys(Node node, Set<K> keys) {
        if (node == null) {
            return;
        }
        collectKeys(node.left, keys);
        keys.add(node.key);
        collectKeys(node.right, keys);
    }

    private class NodeWrapper {
        Node node;
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            return null;
        }
        NodeWrapper wrapper = new NodeWrapper();
        root = remove(root, key, wrapper);
        if (wrapper.node != null) {
            size--;
            return wrapper.node.value;
        }
        return null;
    }

    private Node remove(Node node, K key, NodeWrapper wrapper) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key, wrapper);
        } else if (cmp > 0) {
            node.right = remove(node.right, key, wrapper);
        } else {
            wrapper.node = node;
            if (node.right == null) {
                return node.left;
            }
            if (node.left == null) {
                return node.right;
            }
            Node successor = findMin(node.right);
            successor.right = removeMin(node.right);
            successor.left = node.left;
            return successor;
        }
        return node;
    }

    private Node findMin(Node node) {
        if (node.left == null) {
            return node;
        }
        return findMin(node.left);
    }

    private Node removeMin(Node node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = removeMin(node.left);
        return node;
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) {
            return null;
        }
        Node cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) {
                cur = cur.left;
            } else if (cmp > 0) {
                cur = cur.right;
            } else {
                if (cur.value.equals(value)) {
                    return remove(key);
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
    }

    private class BSTIterator implements Iterator<K> {
        private final Set<K> keys;
        private final Iterator<K> iterator;

        private BSTIterator() {
            keys = keySet();
            iterator = keys.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            return iterator.next();
        }
    }
}
