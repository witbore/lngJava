package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Groups line indices of a file based on shared numeric values at identical column positions.
 * Uses Union-Find to merge lines that share any parsed numeric field.
 */
public class LineGroupAggregator {
    private final Int2ObjectMap<IntList> groups;
    private final int numberOfLines;

    /**
     * Constructs the processor and immediately performs grouping.
     *
     * @param numberOfLines total number of lines in the input file
     * @param index         map from "column;value" key to list of line indices containing that key
     */
    public LineGroupAggregator(int numberOfLines, Int2ObjectMap<IntList> index) {
        this.numberOfLines = numberOfLines;
        this.groups = groupBySharedValues(index);
    }

    /**
     * Performs Union‑Find unions on any lines sharing a “column;value” key,
     * then builds and returns a map from each set‑representative to its
     * list of member line indices.
     *
     * @param index map from "column;value" key to list of line indices containing that key
     * @return map of set representative indexes to the line IDs.
     */
    private Int2ObjectMap<IntList> groupBySharedValues(Int2ObjectMap<IntList> index) {
        IntUnionFindSet unionFindSet = createDisjointSets(numberOfLines);
        unionElements(index, unionFindSet);
        return buildGroupsByRoot(unionFindSet);
    }

    /**
     * Initialises a Union-Find sets for each line as its own root.
     * On the basis of those sets the connection between them will be established.
     *
     * @param size number of sets to be created.
     * @return set representing the lines of the processing file.
     */
    private IntUnionFindSet createDisjointSets(int size) {
        IntUnionFindSet set = new IntUnionFindSet();
        for (int id = 0; id < size; id++) {
            set.createSingleElementSet(id);
        }
        return set;
    }

    /**
     * Merges sets of lines that share the same number at the same column position.
     *
     * @param index map from "column;value" key to list of line indices containing that key
     * @param set   Union-Find set representing the lines of the file
     */
    private void unionElements(Int2ObjectMap<IntList> index, IntUnionFindSet set) {
        for (IntList group : index.values()) {
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
     * Computes the groups of lines which have at least one identical pair 'column;number'
     *
     * @param set Union-Find set which represents the connection between file lines.
     * @return map of set representative indexes to the line IDs.
     */
    private Int2ObjectMap<IntList> buildGroupsByRoot(IntUnionFindSet set) {
        int[] rootCounts = new int[numberOfLines];
        for (int id = 0; id < numberOfLines; id++) {
            int root = set.findSetRootByElement(id);
            rootCounts[root]++;
        }

        Int2ObjectMap<IntList> groups = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < numberOfLines; id++) {
            int root = set.findSetRootByElement(id);
            if (rootCounts[root] < 2){
                continue;
            }
            groups.computeIfAbsent(root, k -> new IntArrayList(rootCounts[root])).add(id);
        }
        return groups;
    }

    /**
     * Filters out groups with less than two elements and sorts remaining groups by their size in descending order.
     *
     * @return groups with more than one element.
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
