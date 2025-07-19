package org.lng.internal;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GroupWriter {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    private final MappedByteBuffer buffer;
    private final long[] lineOffsets;

    public GroupWriter(Path inputFile) {
        try (FileChannel channel = FileChannel.open(inputFile, EnumSet.of(StandardOpenOption.READ))) {
            long size = channel.size();
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            List<Long> offsets = new ArrayList<>();
            offsets.add(0L);
            for (long pos = 0; pos < size; pos++) {
                if (buffer.get((int) pos) == '\n') {
                    long next = pos + 1;
                    if (next < size){
                        offsets.add(next);
                    }
                }
            }
            lineOffsets = new long[offsets.size() + 1];
            for (int i = 0; i < offsets.size(); i++) {
                lineOffsets[i] = offsets.get(i);
            }
            lineOffsets[offsets.size()] = size;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeGroupsToFile(Path outputFile, ObjectList<IntList> sortedGroups) {
        try (PrintWriter writer = new PrintWriter(outputFile.toFile(), StandardCharsets.UTF_8)) {
            writer.println("Всего групп с более чем одним элементом: " + sortedGroups.size());
            writer.println();

            for (int i = 0; i < sortedGroups.size(); i++) {
                writer.println("Группа " + (i + 1));
                IntList group = sortedGroups.get(i);
                group.sort(null);
                for (int lineId : group) {
                    long start = lineOffsets[lineId];
                    long end = lineOffsets[lineId + 1];
                    int length = (int) (end - start);
                    byte[] bytes = new byte[length];
                    buffer.position((int) start);
                    buffer.get(bytes);
                    String line = new String(bytes, CHARSET);
                    if (line.endsWith("\n")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    writer.println(line);
                }
                writer.println();
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
