package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PositionValueIndexer {
    private static List<String> lines = new ArrayList<>();

    public PositionValueIndexer(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lines = Collections.unmodifiableList(lines);
    }

    public static String getLineByIndex(int index) {
        return lines.get(index);
    }

    public static int getNumberOfLines() {
        return lines.size();
    }

    public Int2ObjectMap<Object2ObjectMap<Triple, IntList>> buildIndex() {
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> columnToNumbersWithLineIds = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < lines.size(); id++) {
            String line = lines.get(id);
            ParsedLine parsed = parseLine(line, id);
            if (parsed != null) {
                indexParsedLine(parsed, id, columnToNumbersWithLineIds);
            }
        }
        return columnToNumbersWithLineIds;
    }

    private ParsedLine parseLine(String line, int id) {
        ObjectArrayList<Triple> numbers = new ObjectArrayList<>();
        IntArrayList columns = new IntArrayList();
        int start = 0, column = 0;

        while (start < line.length()) {
            while (start < line.length() && line.charAt(start) != '"') start++;
            if (start >= line.length()) break;

            int end = start + 1;
            while (end < line.length() && line.charAt(end) != '"') end++;
            if (end >= line.length()) break;

            int afterQuote = end + 1;
            if (afterQuote < line.length() && line.charAt(afterQuote) != ';') {
                return null;
            }

            numbers.add(new Triple(id, start, end));
            columns.add(column);
            start = end + 1;
            column++;
        }

        return new ParsedLine(columns, numbers);
    }

    private void indexParsedLine(ParsedLine parsed, int lineId, Int2ObjectMap<Object2ObjectMap<Triple, IntList>> index) {
        for (int i = 0; i < parsed.columns.size(); i++) {
            int col = parsed.columns.getInt(i);
            Triple triple = parsed.triples.get(i);
            Object2ObjectMap<Triple, IntList> valToLines = index.computeIfAbsent(col, k -> new Object2ObjectOpenHashMap<>());
            IntList lineList = valToLines.computeIfAbsent(triple, k -> new IntArrayList());
            lineList.add(lineId);
        }
    }

    private record ParsedLine(IntArrayList columns, ObjectArrayList<Triple> triples) {
    }


    static public class Triple {
        final int lineId;
        final int left;
        final int right;

        public Triple(int lineId, int left, int right) {
            this.lineId = lineId;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Triple other)) return false;
            int len = right - left;
            if (len != other.right - other.left) return false;
            for (int i = 0; i < len; i++) {
                if (lines.get(lineId).charAt(left + i) != lines.get(other.lineId).charAt(other.left + i)) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (int i = left; i < right; i++) {
                result = 31 * result + lines.get(lineId).charAt(i);
            }
            return result;
        }
    }

}
