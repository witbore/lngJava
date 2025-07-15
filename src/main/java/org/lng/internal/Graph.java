package org.lng.internal;

import java.util.*;

public class Graph<T> {
    private final Map<T, Set<T>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addVertex(T vertex) {
        adjacencyList.put(vertex, new HashSet<>());
    }

    public void addEdge(T vertex, T edge) {
        adjacencyList.computeIfAbsent(vertex, k -> new HashSet<>()).add(edge);
    }

    public void addEdge(T vertex, Collection<T> edge) {
        adjacencyList.computeIfAbsent(vertex, k -> new HashSet<>()).addAll(edge);
    }

    public List<List<T>> traverseByBFS() {
        List<List<T>> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        for (T line : adjacencyList.keySet()) {
            if (visited.contains(line)) {
                continue;
            }
            Deque<T> queue = new ArrayDeque<>();
            List<T> group = new ArrayList<>();
            queue.add(line);

            while (!queue.isEmpty()) {
                T current = queue.pop();
                if (!visited.add(current)) {
                    continue;
                }
                group.add(current);
                for (T neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            result.add(group);
        }
        return result;
    }
}
