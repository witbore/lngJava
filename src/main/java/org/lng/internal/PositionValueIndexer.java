package org.lng.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionValueIndexer {
    public static List<String> readFile(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    public static Map<Integer, Map<Pair, List<Integer>>> buildIndex(List<String> lines) {
        Map<Integer, Map<Pair, List<Integer>>> columnToNumbersWithLineIds = new HashMap<>();
        for (int id = 0; id < lines.size(); id++) {
            String line = lines.get(id);
            List<Pair> pairs = new ArrayList<>();
            List<Integer> columnIds = new ArrayList<>();
            int start = 0, column = 0;
            while (start < line.length()) {
                while (start < line.length() && line.charAt(start) != '"') {
                    start++;
                }
                if (start >= line.length()) {
                    break;
                }

                int end = start + 1;
                while (end < line.length() && line.charAt(end) != '"') {
                    end++;
                }

                if (end >= line.length()) {
                    break;
                }

                int afterQuote = end + 1;
                if (afterQuote < line.length() && line.charAt(afterQuote) != ';') {
                    columnIds.removeAll(columnIds);
                    pairs.removeAll(pairs);
                    break;
                }

                Pair number = new Pair(start, end);
                columnIds.add(column);
                pairs.add(number);
                start = end + 1;
                column++;
            }
            for (int i = 0; i < columnIds.size(); i++) {
                Map<Pair, List<Integer>> valToLines = columnToNumbersWithLineIds.computeIfAbsent(columnIds.get(i), k -> new HashMap<>());
                List<Integer> lineList = valToLines.computeIfAbsent(pairs.get(i), k -> new ArrayList<>());
                lineList.add(id);
            }
        }
        return columnToNumbersWithLineIds;
    }

}
