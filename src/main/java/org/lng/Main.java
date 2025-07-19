package org.lng;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.lng.internal.GroupProcessor;
import org.lng.internal.GroupWriter;
import org.lng.internal.InputFileReader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No input file provided");
        }
        File file = new File(args[0]);
        InputFileReader reader = new InputFileReader(file);
        GroupProcessor processor = new GroupProcessor(reader.getLineNumber(), reader.getIndex());
        ObjectList<IntList> multiElementGroups = processor.getFilteredAndSortedGroups();
        Path inputPath = reader.getInputFile().toPath();
        GroupWriter writer = new GroupWriter(inputPath);
        Path outputPath = Paths.get("output.txt");
        writer.writeGroupsToFile(outputPath, multiElementGroups);
    }
}