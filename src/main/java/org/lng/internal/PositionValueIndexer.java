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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionValueIndexer {
    private static final List<String> lines = new ArrayList<>();
    private static final IntList correctLineIds = new IntArrayList();
    private static final Pattern VALID_LINE_PATTERN = Pattern.compile("^\"\\d*\"(?:;\"\\d*\")*$");
    private static final Pattern QUOTED_NUMBER_PATTERN = Pattern.compile("\"(\\d*)\"");

    public PositionValueIndexer(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLineByIndex(int index) {
        return lines.get(index);
    }

    public static IntList getCorrectLineIds() {
        return correctLineIds;
    }

    public Int2ObjectMap<Object2ObjectMap<Triple, IntList>> buildIndex() {
        Int2ObjectMap<Object2ObjectMap<Triple, IntList>> columnToNumbersWithLineIds = new Int2ObjectOpenHashMap<>();
        for (int id = 0; id < lines.size(); id++) {
            String line = lines.get(id);
            ParsedLine parsed = parseLine(line, id);
            if (parsed != null) {
                correctLineIds.add(id);
                indexParsedLine(parsed, id, columnToNumbersWithLineIds);
            }
        }
        return columnToNumbersWithLineIds;
    }

    private ParsedLine parseLine(String line, int id) {
        if (!VALID_LINE_PATTERN.matcher(line).matches()) {
            return null;
        }

        ObjectArrayList<Triple> triples = new ObjectArrayList<>();
        IntArrayList columns = new IntArrayList();

        Matcher matcher = QUOTED_NUMBER_PATTERN.matcher(line);
        int columnIndex = 0;

        while (matcher.find()) {
            int left = matcher.start(1);
            int right = matcher.end(1);
            columns.add(columnIndex);
            triples.add(new Triple(id, left, right));
            columnIndex++;
        }

        return triples.isEmpty() ? null : new ParsedLine(columns, triples);
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
