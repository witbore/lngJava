package org.lng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

record Pair(Integer position, String number) {
}

public class Main {
    public static void main(String[] args) {
        File file = new File("src/main/resources/lng-4.txt");
        Map<Pair, List<String>> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] split = line.split(";");
                for (int pos = 0; pos < split.length; pos++) {
                    String number = split[pos];
                    if (number.length() < 3 || !number.matches("\"\\d+\"")) {
                        continue;
                    }
                    map.computeIfAbsent(new Pair(pos, number), pair -> new ArrayList<>()).add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(map.size());
    }
}