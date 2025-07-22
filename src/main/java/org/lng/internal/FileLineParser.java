package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


final class FileLineParser {
    private static final String NUMBER_DELIMITER = ";";
    private static final char NUMBER_BOUNDARY = '"';
    private static final char NUMBER_DOT = '.';

    /**
     * Splits a CSV line at semicolons and validates numeric substrings.
     * If any field is malformed, parsing stops and an empty map is returned.
     * <p>
     * Examples of valid lines:
     * - "123";"456.78";
     * - "123";"456";"789"
     * <p>
     * Examples of invalid lines (result in empty map):
     * - "12.3.4"2"
     * - "123"456
     *
     * @param line the input line
     * @return a map of column index to substring text for each valid numeric field,
     * or an empty map if any field is invalid
     */
    Int2ObjectMap<String> parseLine(String line) {
        Int2ObjectMap<String> columnsToSubstrings = new Int2ObjectOpenHashMap<>();
        String[] substrings = line.split(NUMBER_DELIMITER);
        for (int column = 0; column < substrings.length; column++) {
            String content = substrings[column];
            int len = content.length();

            // ';;' are valid lines
            if (len == 0) {
                continue;
            }

            if (len >= 2 && content.charAt(0) == NUMBER_BOUNDARY && content.charAt(len - 1) == NUMBER_BOUNDARY) {
                int innerContentStart = 1;
                int innerContentEnd = len - 1;

                // String should be non-empty
                if (innerContentStart == innerContentEnd) {
                    continue;
                }

                boolean hasDot = false;
                boolean isValid = true;
                for (int k = innerContentStart; k < innerContentEnd; k++) {
                    char c = content.charAt(k);

                    if(c == NUMBER_BOUNDARY){
                        // Numbers like "123"456" are invalid
                        isValid = false;
                        break;
                    } else if (c == NUMBER_DOT) {
                        // Decimal numbers are valid: "123.567"
                        if (hasDot) {
                            isValid = false;
                            break;
                        }
                        hasDot = true;
                    } else if (!Character.isDigit(c)) {
                        // Other symbols like ',' are prohibited
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    columnsToSubstrings.put(column, content);
                } else {
                    return new Int2ObjectOpenHashMap<>();
                }
            } else {
                // Lines like ;123; and ;"; are prohibited
                return new Int2ObjectOpenHashMap<>();
            }
        }

        return columnsToSubstrings;
    }
}
