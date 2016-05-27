package no.dv8.utils;

public class Pair<K, V> {

    private final V value;
    private final K key;

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    public Pair(K k, V v ) {
        this.key = k;
        this.value = v;
    }
}
