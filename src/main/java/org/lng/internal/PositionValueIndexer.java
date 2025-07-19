package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.io.*;

public class PositionValueIndexer {
    private final File file;
    private final FileLineParser parser;
    private final File tempFile;

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
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(file))) {

            for (String line = br.readLine(); line != null; line = br.readLine(), lineNumber++) {
                saveToTempFile(lineNumber, parser.parseLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lineNumber;
    }

    private void saveToTempFile(int lineNumber, Int2ObjectMap<String> strings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, true))) {
            for (Int2ObjectMap.Entry<String> entity : strings.int2ObjectEntrySet()) {
                bw.write(lineNumber + ";" + entity.getIntKey() + ";" + entity.getValue() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
