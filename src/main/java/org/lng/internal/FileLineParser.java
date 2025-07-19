package org.lng.internal;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

final class FileLineParser {
    private static final String NUMBER_DELIMITER = ";";
    private static final char NUMBER_BOUNDARY = '"';
    private static final char NUMBER_DOT = '.';

    /**
     * Parses lines for the correct numbers or returns `null`.
     * Correct lines:
     * `"AAA";"AAA"`
     * `"";"AAA"
     * `"AAA.A";"AAA.A"
     * Incorrect ones:
     * `"AAA"AAA"
     * `AAA`
     *
     * @return columns with non-empty Strings
     */
    Int2ObjectMap<String> parseLine(String line) {
        Int2ObjectMap<String> columnsToSubstrings = new Int2ObjectOpenHashMap<>();
        String[] substrings = line.split(NUMBER_DELIMITER);
        for (int column = 0; column < substrings.length; column++) {
            String content = substrings[column];
            int len = content.length();

            if (len == 0) {
                continue;
            }

            if (len >= 2 && content.charAt(0) == NUMBER_BOUNDARY && content.charAt(len - 1) == NUMBER_BOUNDARY) {

                int innerContentStart = 1;
                int innerContentEnd = len - 1;

                if (innerContentStart == innerContentEnd) {
                    continue;
                }

                boolean hasDot = false;
                boolean isValid = true;
                for (int k = innerContentStart; k < innerContentEnd; k++) {
                    char c = content.charAt(k);
                    if (c == NUMBER_DOT) {
                        if (hasDot) {
                            isValid = false;
                            break;
                        }
                        hasDot = true;
                    } else if (!Character.isDigit(c)) {
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
                boolean isValid = true;
                for (int k = 0; k < len; k++) {
                    if (!Character.isDigit(content.charAt(k))) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    columnsToSubstrings.put(column, content);
                } else {
                    return new Int2ObjectOpenHashMap<>();
                }
            }
        }

        return columnsToSubstrings;
    }
}
