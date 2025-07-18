package org.lng;

import org.lng.internal.FileReader;
import org.lng.internal.GroupProcessor;
import org.lng.internal.GroupWriter;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No input file provided");
        }
        File file = new File(args[0]);
        FileReader fileReader = new FileReader(file);
        GroupProcessor processor = new GroupProcessor(fileReader);
        GroupWriter writer = new GroupWriter(fileReader);

        List<List<Integer>> multiElementGroups = processor.processFileLines();
        writer.writeGroupsToFile("output.txt", multiElementGroups);
    }
}