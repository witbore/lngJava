package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class GroupProcessor {

    private final Int2ObjectMap<IntList> groups;
    private final int lineNumber;

    public GroupProcessor(int fileSize, Int2ObjectMap<IntList> index) {
        lineNumber = fileSize;
        groups = processFileLines(index);
    }

    /**
     * Processes the input file to find and group lines sharing common values in the same column positions.
     */
    private Int2ObjectMap<IntList> processFileLines(Int2ObjectMap<IntList> index) {
        IntUnionFindSet unionFindSet = createUnionFindSet(lineNumber);
        unionElements(index, unionFindSet);
        return buildGroups(unionFindSet);
    }

    /**
     * Creates and initialises a Union-Find set structure with each line ID as a separate singleton set.
     */
    private IntUnionFindSet createUnionFindSet(int size) {
        IntUnionFindSet set = new IntUnionFindSet();
        for (int id = 0; id < size; id++) {
            set.createSingleElementSet(id);
        }
        return set;
    }

    /**
     * Merges sets of lines that share the same number at the same column position.
     */
    private void unionElements(Int2ObjectMap<IntList> positionValueToLines, IntUnionFindSet set) {
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
     */
    private Int2ObjectMap<IntList> buildGroups(IntUnionFindSet set) {
        Int2ObjectMap<IntList> groups = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < lineNumber; id++) {
            int root = set.findSetRootByElement(id);
            groups.computeIfAbsent(root, k -> new IntArrayList()).add(id);
        }
        return groups;
    }

    /**
     * Filters out groups with less than two elements and sorts groups by their size in descending order.
     */
    public ObjectList<IntList> getFilteredAndSortedGroups() {
        ObjectList<IntList> multiElementGroups = new ObjectArrayList<>();
        for (IntList group : groups.values()) {
            if (group.size() < 2) {
                continue;
            }
            multiElementGroups.add(group);
        }
        multiElementGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return multiElementGroups;
    }
}
