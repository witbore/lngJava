package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PositionValueIndexer {

    private static final IntList correctLineIds = new IntArrayList();
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
    public static IntList getCorrectLineIds() {
        return correctLineIds;
    }

    /**
     * For each column position maps unique number (as Triple) and list of line IDs containing it.
     * It enables efficient grouping of lines with same `Column` and `Number`.
     * To save memory, this structure stores number positions instead of the actual number or string.
     *
     * @return index `{Column : {<LineId, NumStart, NumEnd> : [LineIds]}`}
     */
    public Int2ObjectMap<Object2ObjectMap<Slice, IntList>> buildIndex() {
        Int2ObjectMap<Object2ObjectMap<Slice, IntList>> columnToNumbersWithLineIds = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < fileReader.getNumberOfLines(); id++) {
            IntList parsed = parseLine(id);
            if (!parsed.isEmpty()) {
                correctLineIds.add(id);
                indexParsedLine(id, parsed, columnToNumbersWithLineIds);
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
    private void indexParsedLine(int lineId, IntList parsed, Int2ObjectMap<Object2ObjectMap<Slice, IntList>> index) {
        for (int i = 0; i < parsed.size() - 2; i++) {
            int column = parsed.getInt(i);
            int start = parsed.getInt(i + 1);
            int end = parsed.getInt(i + 2);
            Object2ObjectMap<Slice, IntList> valToLines = index.computeIfAbsent(column,
                                                                                k -> new Object2ObjectOpenHashMap<>());
            IntList lineList = valToLines.computeIfAbsent(new Slice(lineId, start, end), k -> new IntArrayList());
            lineList.add(lineId);
        }
    }

    /**
     * Represents a substring (number) within a specific line by storing
     * the line ID and the start and end character offsets of the number.
     */
    public class Slice {
        final int lineId;
        final int numberStart;
        final int numberEnd;

        public Slice(int lineId, int numberStart, int numberEnd) {
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
            int result = 1;
            for (int i = numberStart; i < numberEnd; i++) {
                result = 31 * result + fileReader.getLineById(lineId).charAt(i);
            }
            return result;
        }
    }

}
