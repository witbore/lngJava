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
    /**
     * Processes the input file to find and group lines sharing common values in the same column positions.
     *
     * @param file input file to process
     * @return list of groups containing line IDs, each group has at least two lines,
     * sorted by group size in descending order
     */
    public List<List<Integer>> processFile(File file) {
        PositionValueIndexer indexer = new PositionValueIndexer(file);
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> positionValueToLines = indexer.buildIndex();

        IntUnionFindSet unionFindSet = createUnionFindSet(PositionValueIndexer.getCorrectLineIds());

        unionElements(positionValueToLines, unionFindSet);

        Map<Integer, List<Integer>> groups = buildGroups(unionFindSet);

        return filterAndSortGroups(groups);
    }

    /**
     * Creates and initialises a Union-Find set structure with each line ID as a separate singleton set.
     *
     * @param lineIds list of valid line IDs
     * @return initialized Union-Find set
     */
    private IntUnionFindSet createUnionFindSet(IntList lineIds) {
        IntUnionFindSet set = new IntUnionFindSet();
        for (int id : lineIds) {
            set.createSingleElementSet(id);
        }
        return set;
    }

    /**
     * Merges sets of lines that share the same number at the same column position.
     *
     * @param positionValueToLines index mapping columns and numbers to line ID groups
     * @param set                  Union-Find set to perform unions on
     */
    private void unionElements(Int2ObjectMap<Object2ObjectMap<Triple, IntList>> positionValueToLines,
                               IntUnionFindSet set) {
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

    /**
     * Builds groups of line IDs by their Union-Find root element.
     *
     * @param set Union-Find set with unions performed
     * @return map of root element to list of line IDs in that group
     */
    private Map<Integer, List<Integer>> buildGroups(IntUnionFindSet set) {
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int id : PositionValueIndexer.getCorrectLineIds()) {
            int root = set.findSetRootByElement(id);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(id);
        }
        return groups;
    }

    /**
     * Filters out groups with less than two elements and sorts groups by their size in descending order.
     *
     * @param groups map of root elements to groups of line IDs
     * @return sorted list of groups with at least two elements
     */
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
