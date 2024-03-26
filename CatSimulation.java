import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Java simulation of the "cat" command in Unix-like operating systems.
 *
 * SYNOPSIS
 * cat [OPTION]... [FILE]...
 *
 * DESCRIPTION
 * Concatenate FILE(s) to standard output.
 *
 * OPTIONS
 * -n - option adds line numbers to the output
 * -v - option displays non-printable characters as visible representations
 * > and >> - redirection operators for output to files
 */

public class CatSimulation {
    public static void main(String[] args) {
        if (args.length == 0) {

            return;
        }

        boolean numberLines = false;
        boolean showNonPrintable = false;
        boolean redirectOutput = false;
        boolean sortAlphabetically = false;
        boolean appendOutput = false;
        String outputFile = null;
        List<String> inputFiles = new ArrayList<>();

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-n":
                    numberLines = true;
                    break;
                case "-v":
                    showNonPrintable = true;
                    break;
                case "-sa": // Recognizing the new option
                    sortAlphabetically = true;
                    break;
                case ">":
                    if (args.length <= i + 1) {
                        System.out.println("Error: No output file specified");
                        return;
                    }
                    redirectOutput = true;
                    outputFile = args[i + 1];
                    i++;
                    break;
                case ">>":
                    if (args.length <= i + 1) {
                        System.out.println("Error: No output file specified");
                        return;
                    }
                    appendOutput = true;
                    outputFile = args[i + 1];
                    i++;
                    break;
                default:
                    inputFiles.add(args[i]);
            }
            i++;
        }

        List<String> combinedContent = new ArrayList<>();
        for (String filename : inputFiles) {
            combinedContent.addAll(readFileContent(filename));
        }

        if (sortAlphabetically) {
            combinedContent = combinedContent.parallelStream().sorted().collect(Collectors.toList());
        }

        if (numberLines) {
            addLineNumbers(combinedContent);
        }

        if (showNonPrintable) {
            showNonPrintableCharacters(combinedContent);
        }

        if (redirectOutput) {
            writeToFile(outputFile, combinedContent, false);
        } else if (appendOutput) {
            writeToFile(outputFile, combinedContent, true);
        } else {
            for (String line : combinedContent) {
                System.out.println(line);
            }
        }
    }

    public static List<String> readFileContent(String filename) {
        List<String> content = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            content = stream.collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return content;
    }

    public static void addLineNumbers(List<String> content) {
        for (int i = 0; i < content.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(i + 1);
            sb.append(": ");
            sb.append(content.get(i));
            content.set(i, sb.toString());
        }
    }

    public static void showNonPrintableCharacters(List<String> content) {
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i);
            StringBuilder modifiedLine = new StringBuilder(line.length());
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c < 32 || c > 126) {
                    modifiedLine.append("^").append((char) (c + 64));
                } else {
                    modifiedLine.append(c);
                }
            }
            content.set(i, modifiedLine.toString());
        }
    }

    public static void writeToFile(String filename, List<String> content, boolean append) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, append))) {
            for (String line : content) {
                writer.println(line);
            }
            if (append) {
                System.out.println("Content appended to " + filename);
            } else {
                System.out.println("Content written to " + filename);
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
