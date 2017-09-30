package io.gex.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Columns {

    private List<List<String>> lines = new ArrayList<>();
    private List<Integer> maxLengths = new ArrayList<>();
    private int columnSpace = 3;
    private int numColumns = -1;

    Columns addLine(String... line) {
        if (numColumns == -1) {
            numColumns = line.length;
            for (int i = 0; i < numColumns; i++) {
                maxLengths.add(0);
            }
        }
        if (numColumns != line.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < numColumns; i++) {
            maxLengths.set(i, Math.max(maxLengths.get(i), line[i].length()));
        }
        lines.add(Arrays.asList(line));
        return this;
    }

    public void print() {
        System.out.println(toString());
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\t");
        for (List<String> line : lines) {
            for (int i = 0; i < numColumns; i++) {
                result.append(pad(line.get(i), maxLengths.get(i) + 1));
            }
            result.append(System.lineSeparator()).append("\t");
        }
        return result.delete(result.length() - (System.lineSeparator().length() + 1), result.length() - 1).toString();
    }

    private String pad(String word, int newLength) {
        while (word.length() < newLength + columnSpace) {
            word += " ";
        }
        return word;
    }
}
