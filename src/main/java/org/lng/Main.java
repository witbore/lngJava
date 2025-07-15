package org.lng;

import org.lng.internal.Graph;
import org.lng.internal.PositionedValue;
import org.lng.internal.PositionValueIndexer;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        File file = new File("src/main/resources/lng-4.txt");
        Map<PositionedValue, List<String>> positionValueToLines = PositionValueIndexer.buildIndex(file);
        Graph<String> graph = new Graph<>();
        for (Map.Entry<PositionedValue, List<String>> entry : positionValueToLines.entrySet()) {
            List<String> lines = entry.getValue();
            for (String line : lines) {
                graph.addEdge(line, lines);
            }
        }
        List<List<String>> result = graph.traverseByBFS();
        System.out.println(result.size());
    }
}