package org.lng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
                    map.computeIfAbsent(new Pair(pos, number), k -> new ArrayList<>()).add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, Set<String>> adjacencyList = new HashMap<>();
        for(Map.Entry<Pair, List<String>> entry : map.entrySet()) {
            List<String> lines = entry.getValue();
            for(String line : lines) {
                adjacencyList.computeIfAbsent(line, k -> new HashSet<>()).addAll(lines);
            }
        }
        List<List<String>> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for(String line : adjacencyList.keySet()) {
            if(visited.contains(line)) {
                continue;
            }
            Deque<String> queue = new ArrayDeque<>();
            List<String> group = new ArrayList<>();
            queue.add(line);

            while(!queue.isEmpty()) {
                String current = queue.pop();
                if(!visited.add(current)) {
                    continue;
                }
                group.add(current);
                for (String neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }            }
            result.add(group);
        }
        System.out.println(result.size());
    }
}