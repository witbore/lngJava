package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PositionValueIndexer {
    private static final ObjectList<String> fileLines = new ObjectArrayList<>();
    private static final IntList correctLineIds = new IntArrayList();
    private static final char STRING_DELIMITER = ';';
    private static final char NUMBER_BOUNDARY = '"';
    private static final char NUMBER_DOT = '.';

    public PositionValueIndexer(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                fileLines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param index position of the line in the file.
     * @return the line at that position.
     */
    public static String getLineByIndex(int index) {
        return fileLines.get(index);
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
    public Int2ObjectMap<Object2ObjectMap<Triple, IntList>> buildIndex() {
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> columnToNumbersWithLineIds = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < fileLines.size(); id++) {
            String line = fileLines.get(id);
            ParsedLine parsed = parseLine(line, id);
            if (parsed != null) {
                correctLineIds.add(id);
                indexParsedLine(parsed, id, columnToNumbersWithLineIds);
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
    private ParsedLine parseLine(String line, int id) {
        ObjectArrayList<Triple> triples = new ObjectArrayList<>();
        IntArrayList columns = new IntArrayList();

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
                        columns.add(column);
                        triples.add(new Triple(id, numberContentStart, numberContentEnd));
                    } else {
                        return null;
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
                    columns.add(column);
                    triples.add(new Triple(id, substringStart, substringEnd));
                } else {
                    return null;
                }
            }

            column++;
            pos = substringEnd + 1;
        }

        return triples.isEmpty() ? null : new ParsedLine(columns, triples);
    }


    /**
     * Updates the index by associating each parsed number in a given line with its corresponding column and line IDs.
     *
     * @param parsed the parsed line containing columns and number positions
     * @param lineId the ID of the line being indexed
     * @param index  the index structure mapping columns to numbers and their line IDs
     */
    private void indexParsedLine(ParsedLine parsed,
                                 int lineId,
                                 Int2ObjectMap<Object2ObjectMap<Triple, IntList>> index) {
        for (int i = 0; i < parsed.columns.size(); i++) {
            int col = parsed.columns.getInt(i);
            Triple triple = parsed.triples.get(i);
            Object2ObjectMap<Triple, IntList> valToLines = index.computeIfAbsent(col,
                                                                                 k -> new Object2ObjectOpenHashMap<>());
            IntList lineList = valToLines.computeIfAbsent(triple, k -> new IntArrayList());
            lineList.add(lineId);
        }
    }

    private record ParsedLine(IntArrayList columns, ObjectArrayList<Triple> triples) {}

    /**
     * Represents a substring (number) within a specific line by storing
     * the line ID and the start and end character offsets of the number.
     */
    static public class Triple {
        final int lineId;
        final int numberStart;
        final int numberEnd;

        public Triple(int lineId, int numberStart, int numberEnd) {
            this.lineId = lineId;
            this.numberStart = numberStart;
            this.numberEnd = numberEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Triple other)) {
                return false;
            }
            int len = numberEnd - numberStart;
            if (len != other.numberEnd - other.numberStart) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (fileLines.get(lineId).charAt(numberStart + i) != fileLines.get(other.lineId)
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
                result = 31 * result + fileLines.get(lineId).charAt(i);
            }
            return result;
        }
    }

}
