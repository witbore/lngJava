package org.lng.internal;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class GroupWriter {
    private final File file;
    private final long[] lineOffsets;

    public GroupWriter(File inputFile, int lineNumber) {
        this.file = inputFile;
        lineOffsets = new long[lineNumber + 1];
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long offset = 0L;
            lineOffsets[count++] = offset;
            while (raf.readLine() != null) {
                offset = raf.getFilePointer();
                lineOffsets[count++] = offset;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to build line offsets", e);
        }
    }

    public void writeGroupsToFile(String outputFilename, ObjectList<IntList> sortedGroups) {
        try (PrintWriter writer = new PrintWriter(outputFilename, StandardCharsets.UTF_8);
             RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            writer.println("Всего групп с более чем одним элементом: " + sortedGroups.size());
            writer.println();

            for (int i = 0; i < sortedGroups.size(); i++) {
                writer.println("Группа " + (i + 1));

                IntList group = sortedGroups.get(i);
                group.sort(null); // sort line IDs to minimize random seeks

                for (int lineId : group) {
                    long lineOffset = lineOffsets[lineId];
                    raf.seek(lineOffset);
                    String line = raf.readLine();
                    writer.println(line);
                }
                writer.println();
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed writing groups", e);
        }
    }
}
