package org.lng.internal;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class PositionValueIndexer {

    private static final char NUMBER_BOUNDARY = '"';
    private static final char NUMBER_DOT = '.';
    private final File file;
    private final IntList incorrectIds = new IntArrayList();
    private final ObjectList<String[]> fileLines = new ObjectArrayList<>();
    private final Object2IntMap<String> lineIdsCache = new Object2IntOpenHashMap<>();
    private int lastLineId = 0;

    public PositionValueIndexer(File file) {
        this.file = file;
    }

    public int getNumberOfLines(){
        return fileLines.size();
    }

    public String[] getLine(int lineId){
        return fileLines.get(lineId);
    }

    /**
     * Because some string can contain the invalid data, the correct line IDs are stored to this array.
     *
     * @return line id with correct number representation.
     */
    public IntList getIncorrectIds() {
        return incorrectIds;
    }

    /**
     * For each column position maps unique number (as Triple) and list of line IDs containing it.
     * It enables efficient grouping of lines with same `Column` and `Number`.
     * To save memory, this structure stores number positions instead of the actual number or string.
     *
     * @return index `{Column : {<LineId, NumStart, NumEnd> : [LineIds]}`}
     */
    public Object2ObjectMap<Slice, IntList> buildIndex() {
        Object2ObjectMap<Slice, IntList> columnToNumbersWithLineIds = new Object2ObjectOpenHashMap<>();
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] substrings = line.split(";");
                IntList parsed = parseLine(substrings);
                if (!parsed.isEmpty()) {
                    for (int column : parsed) {
                        lineIdsCache.computeIfAbsent(substrings[column], v -> lastLineId++);
                        indexParsedLine(fileLines.size(),
                                        column,
                                        lineIdsCache.getInt(substrings[column]),
                                        columnToNumbersWithLineIds);
                    }
                } else {
                    incorrectIds.add(fileLines.size());
                }
                fileLines.add(substrings);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return columnToNumbersWithLineIds;
    }

    /**
     * Parses lines for the correct numbers or returns `null`.
     * Correct lines:
     * `"AAA";"AAA"`
     * `"";"AAA"
     * `"AAA.A";"AAA.A"
     * Incorrect ones:
     * `"AAA"AAA"
     * `AAA`
     *
     * @return columns with parsed numbers in `Triple` format or `null` if the line is incorrect.
     */
    private IntList parseLine(String[] substrings) {
        IntArrayList columnsToReturn = new IntArrayList();

        for (int column = 0; column < substrings.length; column++) {
            String content = substrings[column];
            int len = content.length();

            if (len == 0) {
                continue;
            }

            if (len >= 2 && content.charAt(0) == NUMBER_BOUNDARY && content.charAt(len - 1) == NUMBER_BOUNDARY) {

                int innerContentStart = 1;
                int innerContentEnd = len - 1;

                if (innerContentStart == innerContentEnd) {
                    continue;
                }

                boolean hasDot = false;
                boolean isValid = true;
                for (int k = innerContentStart; k < innerContentEnd; k++) {
                    char c = content.charAt(k);
                    if (c == NUMBER_DOT) {
                        if (hasDot) {
                            isValid = false;
                            break;
                        }
                        hasDot = true;
                    } else if (!Character.isDigit(c)) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    columnsToReturn.add(column);
                } else {
                    return new IntArrayList();
                }

            } else {
                boolean isValid = true;
                for (int k = 0; k < len; k++) {
                    if (!Character.isDigit(content.charAt(k))) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    columnsToReturn.add(column);
                } else {
                    return new IntArrayList();
                }
            }
        }

        return columnsToReturn;
    }

    /**
     * Updates the index by associating each parsed number in a given line with its corresponding column and line IDs.
     *
     * @param lineId the ID of the line being indexed
     * @param index  the index structure mapping columns to numbers and their line IDs
     */
    private void indexParsedLine(int lineId, int column, int numberId, Object2ObjectMap<Slice, IntList> index) {
        IntList lineList = index.computeIfAbsent(new Slice(column, numberId), k -> new IntArrayList());
        lineList.add(lineId);
    }

    /**
     * Represents a substring (number) within a specific line by storing
     * the line ID and the start and end character offsets of the number.
     */
    public class Slice {
        final int columnId;
        final int numberId;

        public Slice(int columnId, int numberId) {
            this.columnId = columnId;
            this.numberId = numberId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Slice other)) {
                return false;
            }
            if (columnId != other.columnId) {
                return false;
            }
            return numberId == other.numberId;
        }

        @Override
        public int hashCode() {
            return columnId * 31 + numberId;
        }
    }

}
