package org.lng;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.lng.internal.GroupProcessor;
import org.lng.internal.GroupWriter;
import org.lng.internal.InputFileReader;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No input file provided");
        }
        File file = new File(args[0]);
        InputFileReader reader = new InputFileReader(file);
        GroupProcessor processor = new GroupProcessor(reader.getLineNumber(), reader.getIndex());
        ObjectList<IntList> multiElementGroups = processor.getFilteredAndSortedGroups();
        GroupWriter writer = new GroupWriter(reader.getInputFile(), reader.getLineNumber());
        writer.writeGroupsToFile("output.txt", multiElementGroups);
    }
}