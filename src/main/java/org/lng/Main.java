package org.lng;

import org.lng.internal.GroupProcessor;
import org.lng.internal.GroupWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No input file provided");
        }
        File file = new File(args[0]);
        GroupProcessor processor = new GroupProcessor(file);
        GroupWriter writer = new GroupWriter();

        List<List<Integer>> multiElementGroups = new ArrayList<>();
        writer.writeGroupsToFile("output.txt", multiElementGroups);
    }
}