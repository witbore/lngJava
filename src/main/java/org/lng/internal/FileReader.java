package org.lng.internal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileReader {
    private final ObjectList<String[]> fileLines = new ObjectArrayList<>();

    public FileReader(File file) {
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                fileLines.add(line.split(";"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getLineById(int id) {
        return fileLines.get(id);
    }

    public int getNumberOfLines() {
        return fileLines.size();
    }
}
