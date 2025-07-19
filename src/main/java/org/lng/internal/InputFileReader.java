package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.File;

public class InputFileReader {
    private final int lineNumber;
    private final PositionValueIndexer indexer;
    private final File inputFile;

    public InputFileReader(File file) {
        inputFile = file;
        indexer = new PositionValueIndexer(file);
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
