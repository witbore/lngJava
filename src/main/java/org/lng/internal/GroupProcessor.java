package org.lng.internal;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.lng.internal.PositionValueIndexer.Slice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProcessor {
    private final FileReader reader;
    private IntList incorrectIds;

    public GroupProcessor(FileReader fileReader) {
        reader = fileReader;
        incorrectIds = new IntArrayList();
    }

    /**
     * Processes the input file to find and group lines sharing common values in the same column positions.
     *
     * @return list of groups containing line IDs, each group has at least two lines,
     * sorted by group size in descending order
     */
    public List<List<Integer>> processFileLines() {
        PositionValueIndexer indexer = new PositionValueIndexer(reader);
        Object2ObjectMap<Slice, IntList> positionValueToLines = indexer.buildIndex();
        incorrectIds = indexer.getIncorrectIds();
        IntUnionFindSet unionFindSet = createUnionFindSet(reader.getNumberOfLines());

        unionElements(positionValueToLines, unionFindSet);

        Map<Integer, List<Integer>> groups = buildGroups(unionFindSet);

        return filterAndSortGroups(groups);
    }

    /**
     * Creates and initialises a Union-Find set structure with each line ID as a separate singleton set.
     *
     * @param size number of lines in the file
     * @return initialized Union-Find set
     */
    private IntUnionFindSet createUnionFindSet(int size) {
        IntUnionFindSet set = new IntUnionFindSet();
        for (int id = 0; id < size; id++) {
            if (incorrectIds.contains(id)) {
                continue;
            }
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
    private void unionElements(Object2ObjectMap<Slice, IntList> positionValueToLines, IntUnionFindSet set) {
        for (IntList group : positionValueToLines.values()) {
            if (group.size() < 2) {
                continue;
            }
            int representative = group.getInt(0);
            for (int i = 1; i < group.size(); i++) {
                set.unionSetsByElements(representative, group.getInt(i));
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
        for (int id = 0; id < reader.getNumberOfLines(); id++) {
            if (incorrectIds.contains(id)) {
                continue;
            }
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
            if (group.size() < 2) {
                continue;
            }
            multiElementGroups.add(group);
        }
        multiElementGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return multiElementGroups;
    }
}
