package org.lng.internal;

import java.util.HashMap;
import java.util.Map;

public class UnionFindSetByRank<T extends Comparable<T>> implements UnionFindSet<T> {
    private final HashMap<T, T> roots;
    private final HashMap<T, Integer> ranks;

    public UnionFindSetByRank() {
        roots = new HashMap<>();
        ranks = new HashMap<>();
    }

    @Override
    public void createSingleElementSet(T element) {
        roots.put(element, element);
        ranks.put(element, 0);
    }

    @Override
    public void unionSetsByElements(T first, T second) {
        T firstRoot = findSetRootByElement(first);
        T secondRoot = findSetRootByElement(second);
        if (firstRoot.equals(secondRoot)) {
            if (ranks.get(firstRoot).compareTo(ranks.get(secondRoot)) < 0) {
                roots.put(firstRoot, secondRoot);
            } else if (ranks.get(firstRoot).compareTo(ranks.get(secondRoot)) > 0) {
                roots.put(secondRoot, firstRoot);
            } else {
                roots.put(secondRoot, firstRoot);
                ranks.put(firstRoot, ranks.get(firstRoot) + 1);
            }
        }
    }

    @Override
    public T findSetRootByElement(T element) {
        if (element.equals(roots.get(element))) {
            return element;
        }
        T root = findSetRootByElement(roots.get(element));
        roots.put(element, root);
        return root;
    }

    @Override
    public Map<T, T> getRoots() {
        return roots;
    }
}
