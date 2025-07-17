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
    private static boolean validateFileLine(String line) {
        return line.length() >= 3 && line.matches("\"\\d+\"");
    }

    public static Map<PositionedValue, List<Integer>> buildIndex(File file) {
        Map<PositionedValue, List<Integer>> columnValueMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] split = line.split(";");
                for (int column = 0; column < split.length; column++) {
                    String number = split[column];
                    if (!validateFileLine(number)) {
                        continue;
                    }
                    PositionedValue val = new PositionedValue(column, number);
                    columnValueMap.computeIfAbsent(val, k -> new ArrayList<>()).add(line.hashCode());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return columnValueMap;
    }
}
