package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.*;

public class PositionValueIndexer {
    private final File file;
    private final FileLineParser parser;
    private final File tempFile;
    private final Object2IntMap<String> cache = new Object2IntOpenHashMap<>();

    public PositionValueIndexer(File file) {
        this.file = file;
        this.parser = new FileLineParser();
        try {
            tempFile = File.createTempFile("index_", ".tmp");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int buildIndex() {
        int lineNumber = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = raf.readLine()) != null) {
                Int2ObjectMap<String> parsedNumbers = parser.parseLine(line);
                for (var entity : parsedNumbers.int2ObjectEntrySet()) {
                    bw.write(entity.getIntKey() + ";" + entity.getValue() + "|" + lineNumber + "\n");
                }
                lineNumber++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lineNumber;
    }


    public Int2ObjectMap<IntList> getIndex() {
        Int2ObjectMap<IntList> index = new Int2ObjectOpenHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 2) {
                    continue;
                }
                int key = cache.computeIfAbsent(parts[0], p -> cache.size());
                int lineId = Integer.parseInt(parts[1]);

                index.computeIfAbsent(key, k -> new IntArrayList()).add(lineId);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return index;
    }

}