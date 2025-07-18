package org.lng.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GroupWriter {
    private final FileReader reader;

    public GroupWriter(FileReader fileReader) {
        reader = fileReader;
    }

    public void writeGroupsToFile(String filename, List<List<Integer>> sortedGroups) {
        try (PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8)) {
            writer.println("Всего групп с более чем одним элементом: " + sortedGroups.size());
            writer.println();

            for (int i = 0; i < sortedGroups.size(); i++) {
                writer.println("Группа " + (i + 1));
                for (int lineId : sortedGroups.get(i)) {
                    writer.println(Arrays.toString(reader.getLineById(lineId)));
                }
                writer.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
