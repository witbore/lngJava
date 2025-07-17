package org.lng;


import org.lng.internal.Pair;
import org.lng.internal.PositionValueIndexer;
import org.lng.internal.UnionFindSet;
import org.lng.internal.UnionFindSetByRank;

import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        File file = new File(args[0]);
        Map<Integer, Map<Pair, List<Integer>>> positionValueToLines = PositionValueIndexer.buildIndex(PositionValueIndexer.readFile(file));
        UnionFindSet<Integer> set = new UnionFindSetByRank<>();

        for (Map<Pair, List<Integer>> numToLines : positionValueToLines.values()) {
            for (List<Integer> group : numToLines.values()) {
                if (group.size() < 2) {
                    continue;
                }
                Integer representative = group.get(0);
                set.createSingleElementSet(representative);
                for (int i = 1; i < group.size(); i++) {
                    set.createSingleElementSet(group.get(i));
                    set.unionSetsByElements(representative, group.get(i));
                }
            }
        }

        System.out.println(set.getRoots().size());
    }
}