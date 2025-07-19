package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.File;

public class InputFileReader {
    private final PositionValueIndexer indexer;
    private final File inputFile;
    private final int lineNumber;

    public InputFileReader(File file) {
        inputFile = file;
        indexer = new PositionValueIndexer(file.toPath());
        lineNumber = indexer.buildIndex();
    }

    public File getInputFile() {
        return inputFile;
    }

    public Int2ObjectMap<IntList> getIndex() {
        return indexer.getIndex();
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
