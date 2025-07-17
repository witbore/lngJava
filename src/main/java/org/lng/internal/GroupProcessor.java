package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.lng.internal.PositionValueIndexer.Triple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProcessor {
    public List<List<Integer>> processFile(File file) {
        PositionValueIndexer indexer = new PositionValueIndexer(file);
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> positionValueToLines = indexer.buildIndex();

        IntUnionFindSet unionFindSet = createUnionFindSet(PositionValueIndexer.getCorrectLineIds());

        unionElements(positionValueToLines, unionFindSet);

        Map<Integer, List<Integer>> groups = buildGroups(unionFindSet);

        return filterAndSortGroups(groups);
    }

    private IntUnionFindSet createUnionFindSet(IntList lineIds) {
        IntUnionFindSet set = new IntUnionFindSet();
        for (int id : lineIds) {
            set.createSingleElementSet(id);
        }
        return set;
    }

    private void unionElements(Int2ObjectMap<Object2ObjectMap<Triple, IntList>> positionValueToLines, IntUnionFindSet set) {
        for (Object2ObjectMap<Triple, IntList> numToLines : positionValueToLines.values()) {
            for (List<Integer> group : numToLines.values()) {
                if (group.size() < 2) continue;
                Integer representative = group.get(0);
                for (int i = 1; i < group.size(); i++) {
                    set.unionSetsByElements(representative, group.get(i));
                }
            }
        }
    }

    private Map<Integer, List<Integer>> buildGroups(IntUnionFindSet set) {
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int id : PositionValueIndexer.getCorrectLineIds()) {
            int root = set.findSetRootByElement(id);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(id);
        }
        return groups;
    }

    private List<List<Integer>> filterAndSortGroups(Map<Integer, List<Integer>> groups) {
        List<List<Integer>> multiElementGroups = new ArrayList<>();
        for (List<Integer> group : groups.values()) {
            if (group.size() < 2) continue;
            multiElementGroups.add(group);
        }
        multiElementGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return multiElementGroups;
    }
}
