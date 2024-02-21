package com.buschmais.jqassistant.core.rule.impl.reader;

public class IndentHelper {
    /**
     * This class make the String more effective and understandable. by removing the extra spaces,lines and tabs.
     * even tho , there is some exceptions where if the tabs are not in order the whole class won't work!
     * NOTE : Check the IndentHelperTest.java to see the examples
     *
     * @param text is the String or Description form the file or the input.
     * @return edited text without the unused whitespaces
     */
    public static String removeIndent(String text) {
        if (text == null) {
            return null;
        }
        String textWithoutEmptyLines = removeBlankLeadingAndTrailingLines(text);
        String[] lines = textWithoutEmptyLines.split("\\n");
        if (text.isBlank()) {
            return textWithoutEmptyLines;
        }
        int indent = getIndent(lines);
        return removeIndent(lines, indent);
    }

    /**
     * Removes the specified indentation from each line in the provided array of lines. If it's needed.
     * Removes the trailing and leading spaces from each lines
     * HINT : only indent is considered which contains exactly the same characters across all lines ignoring blank lines.
     * @param lines  An array of strings representing lines of text
     * @param indent The number of leading whitespace characters to remove from each line
     * @return The text with indentation removed
     */

    private static String removeIndent(String[] lines, int indent) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            // We replace all trailing whitespaces from each line.
            String line = lines[i].replaceAll("\\s+$", "");
            if (!line.isBlank()) {
                resultBuilder.append(line.substring(indent));
            } else {
                resultBuilder.append(lines[i]);
            }
            if (i < lines.length - 1) {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();

    }

    /**
     * the indent of a text block is determined by identifying the sequence
     * of blank characters that is shared/the same across all lines of the text block.
     * @param lines
     * @return the count which is currentColumn.
     */

    private static int getIndent(String[] lines) {
        int currentColumn = 0;
        while (true) {
            String prevChar = null;
            for (String line : lines) {
                if (!line.isBlank()) {
                    if (currentColumn == line.length()) {
                        return currentColumn;
                    }
                    String currentChar = line.substring(currentColumn, currentColumn + 1);
                    if (!currentChar.isBlank() || (prevChar != null && !prevChar.equals(currentChar))) {
                        return currentColumn;
                    }
                    prevChar = currentChar;
                }
            }
            currentColumn++;
        }

    }

    /**
     * This is method it's works by reading  the text from the top then from the bottom by each line
     * and see where is the text. if there is a empty lines before or after the text , then this method going to remove it.
     *
     * @param text
     * @return text without trailing and leading  empty lines
     */

    private static String removeBlankLeadingAndTrailingLines(String text) {
        String[] lines = text.split("\\n");

        // read the text from the top and give it a value, so we can print from this value.
        int startIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                startIndex = i;
                break;
            }
        }

        // opposite to the last reader , this reads the text from the bottom to top, also give it a value.
        int lastIndex = lines.length - 1;
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].isBlank()) {
                lastIndex = i;
                break;
            }
        }
        // Here we print the text which is between startIndex and lastIndex, and with that we lose the empty leading trailing lines.
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = startIndex; i <= lastIndex; i++) {
            resultBuilder.append(lines[i]);
            if (i < lastIndex) {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();
    }
}
