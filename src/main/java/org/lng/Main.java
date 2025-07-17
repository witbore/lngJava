package org.lng;


import org.lng.internal.PositionValueIndexer;
import org.lng.internal.PositionedValue;
import org.lng.internal.UnionFindSet;
import org.lng.internal.UnionFindSetByRank;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        File file = new File(args[0]);

        Map<PositionedValue, List<Integer>> positionValueToLines = PositionValueIndexer.buildIndex(file);
        UnionFindSet<Integer> set = new UnionFindSetByRank<>();

        for (List<Integer> group : positionValueToLines.values()) {
            for (Integer id : group) {
                set.createSingleElementSet(id);
            }
        }

        for (List<Integer> lineIds : positionValueToLines.values()) {
            if (lineIds.size() < 2) continue;
            Integer representative = lineIds.get(0);
            for (int i = 1; i < lineIds.size(); i++) {
                set.unionSetsByElements(representative, lineIds.get(i));
            }
        }
        System.out.println(set.getRoots().size());
    }
}