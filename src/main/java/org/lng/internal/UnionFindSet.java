package org.lng.internal;

import java.util.Map;

public interface UnionFindSet<T extends Comparable<T>> {
    void createSingleElementSet(T element);
    void unionSetsByElements(T first, T second);
    T findSetRootByElement(T element);
    Map<T, T> getRoots();
}
