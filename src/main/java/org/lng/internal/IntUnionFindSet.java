package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class IntUnionFindSet {
    private final Int2IntMap roots;
    private final Int2IntMap ranks;

    public IntUnionFindSet() {
        roots = new Int2IntOpenHashMap();
        ranks = new Int2IntOpenHashMap();
    }

    public void createSingleElementSet(int element) {
        roots.put(element, element);
        ranks.put(element, 0);
    }

    public void unionSetsByElements(int first, int second) {
        int firstRoot = findSetRootByElement(first);
        int secondRoot = findSetRootByElement(second);

        if (firstRoot == secondRoot) {
            return;
        }

        int rank1 = ranks.get(firstRoot);
        int rank2 = ranks.get(secondRoot);

        if (rank1 < rank2) {
            roots.put(firstRoot, secondRoot);
        } else if (rank1 > rank2) {
            roots.put(secondRoot, firstRoot);
        } else {
            roots.put(secondRoot, firstRoot);
            ranks.put(firstRoot, rank1 + 1);
        }
    }

    public int findSetRootByElement(int element) {
        int parent = roots.get(element);
        if (element != parent) {
            int root = findSetRootByElement(parent);
            roots.put(element, root);
            return root;
        }
        return element;
    }
}
