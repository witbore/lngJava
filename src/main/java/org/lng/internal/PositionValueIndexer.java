package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Indexes a text file by mapping 'column; number' pairs to the line numbers they occur on.
 * Uses an intermediate temporary file to store the raw mapping.
 */
public class PositionValueIndexer {
    public static final String COLUMN_NUMBER_DELIMITER = ";";
    public static final String LINE_ID_DELIMITER = "\\|";
    public static final String TEMP_FILE_PREFIX = "index_";
    public static final String TEMP_FILE_SUFFIX = ".tmp";
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private final Path filePath;
    private final FileLineParser parser;
    private final Path tempFile;
    private final Object2IntMap<String> cache = new Object2IntOpenHashMap<>();

    /**
     * Creates a new indexer for the given input file. Initialises a temporary file for index storage.
     *
     * @param filePath path to the input file to be indexed
     * @throws RuntimeException if the temporary file cannot be created
     */
    public PositionValueIndexer(Path filePath) {
        this.filePath = filePath;
        this.parser = new FileLineParser();
        try {
            tempFile = Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            tempFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create temporary file", e);
        }
    }

    /**
     * Parses the input file and writes intermediate index data to a temporary file.
     * Each non-empty line is parsed into 'column ; number | line index' mapping.
     *
     * @return total number of lines processed in the file.
     * @throws RuntimeException on I/O failure or malformed data
     */
    public int buildIndex() {
        int lineNumber = 0;
        try (var reader = Files.newBufferedReader(filePath, CHARSET);
             BufferedWriter bw = Files.newBufferedWriter(tempFile,
                                                         CHARSET,
                                                         StandardOpenOption.WRITE,
                                                         StandardOpenOption.TRUNCATE_EXISTING)) {

            String line;
            while ((line = reader.readLine()) != null) {
                Int2ObjectMap<String> parsed = parser.parseLine(line);
                if (!parsed.isEmpty()) {
                    for (var entry : parsed.int2ObjectEntrySet()) {
                        bw.write(entry.getIntKey());
                        bw.write(COLUMN_NUMBER_DELIMITER);
                        bw.write(entry.getValue());
                        bw.write(LINE_ID_DELIMITER);
                        bw.write(Integer.toString(lineNumber));
                        bw.newLine();
                    }
                }
                lineNumber++;
            }
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unable to perform reading from given file and writing to the index file", e);
        }
        return lineNumber;
    }

    /**
     * Builds and returns an in-memory index from the intermediate file.
     * To improve performance keys are represented as int IDs instead of the string 'column;number';
     * The actual strings are stored in a cache.
     *
     * @return map from compacted 'column;value' keys to lists of line numbers
     * @throws RuntimeException if index reading fails
     */
    public Int2ObjectMap<IntList> getIndex() {
        Int2ObjectMap<IntList> index = new Int2ObjectOpenHashMap<>();
        try (var reader = Files.newBufferedReader(tempFile, CHARSET)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(LINE_ID_DELIMITER);
                if (parts.length != 2) {
                    continue;
                }
                int key = cache.computeIfAbsent(parts[0], k -> cache.size());
                index.computeIfAbsent(key, k -> new IntArrayList()).add(Integer.parseInt(parts[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read index file", e);
        }
        return index;
    }
}
