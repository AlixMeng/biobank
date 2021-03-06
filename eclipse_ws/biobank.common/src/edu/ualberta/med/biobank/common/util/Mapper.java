package edu.ualberta.med.biobank.common.util;

public interface Mapper<T, K, V> {
    public K getKey(T type);

    public V getValue(T type, V oldValue);
}