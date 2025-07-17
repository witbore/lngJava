package org.lng;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.lng.internal.IntUnionFindSet;
import org.lng.internal.PositionValueIndexer;
import org.lng.internal.PositionValueIndexer.Triple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        File file = new File(args[0]);
        PositionValueIndexer indexer = new PositionValueIndexer(file);
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> positionValueToLines = indexer.buildIndex();
        IntUnionFindSet set = new IntUnionFindSet();

        for (int id : PositionValueIndexer.getCorrectLineIds()) {
            set.createSingleElementSet(id);
        }

        for (Object2ObjectMap<Triple, IntList> numToLines : positionValueToLines.values()) {
            for (List<Integer> group : numToLines.values()) {
                if (group.size() < 2) continue;

                Integer representative = group.get(0);
                for (int i = 1; i < group.size(); i++) {
                    set.unionSetsByElements(representative, group.get(i));
                }
            }
        }

        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int id : PositionValueIndexer.getCorrectLineIds()) {
            int root = set.findSetRootByElement(id);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(id);
        }
        List<List<Integer>> sortedGroups = new ArrayList<>();

        sortedGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));

        System.out.println(sortedGroups.size());
        for (int i = 0; i < sortedGroups.size(); i++) {
            System.out.println("Group#" + (i + 1));
            for (int lineId : sortedGroups.get(i)) {
                System.out.println(PositionValueIndexer.getLineByIndex(lineId));
            }
            System.out.println();
        }

        System.out.println("Total number of groups: " + groups.size());
    }
}