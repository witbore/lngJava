package org.lng.internal;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class PositionValueIndexer {

    private final IntList incorrectIds = new IntArrayList();
    private static final char STRING_DELIMITER = ';';
    private static final char NUMBER_BOUNDARY = '"';
    private static final char NUMBER_DOT = '.';
    private final FileReader fileReader;

    public PositionValueIndexer(FileReader reader) {
        fileReader = reader;
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
        for (int id = 0; id < fileReader.getNumberOfLines(); id++) {
            IntList parsed = parseLine(id);
            if (!parsed.isEmpty()) {
                indexParsedLine(id, parsed, columnToNumbersWithLineIds);
            } else {
                incorrectIds.add(id);
            }
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
    private IntList parseLine(int lineId) {
        String line = fileReader.getLineById(lineId);
        IntArrayList columnsWithSlices = new IntArrayList();

        int len = line.length();
        int pos = 0;
        int column = 0;

        while (pos < len) {
            int substringEnd = line.indexOf(STRING_DELIMITER, pos);
            if (substringEnd == -1) {
                substringEnd = len;
            }
            int substringStart = pos;
            if (substringStart < substringEnd && line.charAt(substringStart) == NUMBER_BOUNDARY
                    && line.charAt(substringEnd - 1) == NUMBER_BOUNDARY) {

                int numberContentStart = substringStart + 1;
                int numberContentEnd = substringEnd - 1;

                if (numberContentStart != numberContentEnd) {
                    boolean hasDot = false;
                    boolean isValidNumberContent = true;
                    for (int k = numberContentStart; k < numberContentEnd; k++) {
                        char c = line.charAt(k);
                        if (c == NUMBER_DOT) {
                            if (hasDot) {
                                isValidNumberContent = false;
                                break;
                            }
                            hasDot = true;
                        } else if (!Character.isDigit(c)) {
                            isValidNumberContent = false;
                            break;
                        }
                    }

                    if (isValidNumberContent) {
                        columnsWithSlices.add(column);
                        columnsWithSlices.add(numberContentStart);
                        columnsWithSlices.add(numberContentEnd);
                    } else {
                        return new IntArrayList();
                    }
                }
            } else if (substringStart != substringEnd) {
                boolean isValidUnquotedNumber = true;
                for (int k = substringStart; k < substringEnd; k++) {
                    if (!Character.isDigit(line.charAt(k))) {
                        isValidUnquotedNumber = false;
                        break;
                    }
                }

                if (isValidUnquotedNumber) {
                    columnsWithSlices.add(column);
                    columnsWithSlices.add(substringStart);
                    columnsWithSlices.add(substringEnd);
                } else {
                    return new IntArrayList();
                }
            }

            column++;
            pos = substringEnd + 1;
        }

        return columnsWithSlices;
    }


    /**
     * Updates the index by associating each parsed number in a given line with its corresponding column and line IDs.
     *
     * @param parsed the parsed line containing columns and number positions
     * @param lineId the ID of the line being indexed
     * @param index  the index structure mapping columns to numbers and their line IDs
     */
    private void indexParsedLine(int lineId, IntList parsed, Object2ObjectMap<Slice, IntList> index) {
        for (int i = 0; i < parsed.size() - 2; i++) {
            int column = parsed.getInt(i);
            int start = parsed.getInt(i + 1);
            int end = parsed.getInt(i + 2);
            IntList lineList = index.computeIfAbsent(new Slice(column, lineId, start, end), k -> new IntArrayList());
            lineList.add(lineId);
        }
    }

    /**
     * Represents a substring (number) within a specific line by storing
     * the line ID and the start and end character offsets of the number.
     */
    public class Slice {
        final int columnId;
        final int lineId;
        final int numberStart;
        final int numberEnd;

        public Slice(int columnId, int lineId, int numberStart, int numberEnd) {
            this.columnId = columnId;
            this.lineId = lineId;
            this.numberStart = numberStart;
            this.numberEnd = numberEnd;
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
            int len = numberEnd - numberStart;
            if (len != other.numberEnd - other.numberStart) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (fileReader.getLineById(lineId).charAt(numberStart + i) != fileReader.getLineById(other.lineId)
                        .charAt(other.numberStart + i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            String line = fileReader.getLineById(lineId);
            int hash = 0x811c9dc5;
            hash ^= columnId;
            hash *= 0x01000193;
            hash ^= numberStart;
            hash *= 0x01000193;

            for (int i = numberStart; i < numberEnd; i++) {
                hash ^= line.charAt(i);
                hash *= 0x01000193;
            }
            return hash;
        }
    }

}
