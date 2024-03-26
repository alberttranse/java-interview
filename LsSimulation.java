import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * A Java simulation of the "ls" command in Unix-like operating systems.
 *
 * SYNOPSIS
 * ls [OPTION]... [FILE]...
 *
 * DESCRIPTION
 * List information about the FILEs (the current directory by default).
 *
 * OPTIONS
 * -a - do not ignore file names that start with ‘.’.
 * -A - do not ignore all file names that start with ‘.’; ignore only . and ...The -a option overrides this option.
 * -d - list just the names of directories, as with other types of files, rather than listing their contents
 * -F - append a character to each file name indicating the file type (/*@)
 * -l - use a long listing format
 * -r - reverse order while sorting
 * -S - sort by file size, largest first
 * -t - sort by time, newest first
 */

public class LsSimulation {
    private enum SortBy {
        TIME,
        SIZE,
        NAME,
        NONE
    }

    private static final Map<String, String> FILE_TYPE_INDICATOR = new HashMap<>() {
        {
            put("DIRECTORY", "/");
            put("EXECUTABLE", "*");
            put("SYMBOLIC_LINK", "@");
        }
    };

    private static final List<Map.Entry<PosixFilePermission, String>> PERMISSION_LIST = List.of(
            Map.entry(PosixFilePermission.OWNER_READ, "r"),
            Map.entry(PosixFilePermission.OWNER_WRITE, "w"),
            Map.entry(PosixFilePermission.OWNER_EXECUTE, "x"),
            Map.entry(PosixFilePermission.GROUP_READ, "r"),
            Map.entry(PosixFilePermission.GROUP_WRITE, "w"),
            Map.entry(PosixFilePermission.GROUP_EXECUTE, "x"),
            Map.entry(PosixFilePermission.OTHERS_READ, "r"),
            Map.entry(PosixFilePermission.OTHERS_WRITE, "w"),
            Map.entry(PosixFilePermission.OTHERS_EXECUTE, "x"));

    private static final Map<String, String> ERRORS = new HashMap<>() {
        {
            put("INVALID_OPTION", "Invalid option: %s");
            put("NO_DIRECTORY", "%s: No such file or directory");
        }
    };

    private static final Map<String, Runnable> OPTION_ACTIONS = new HashMap<>();
    static {
        OPTION_ACTIONS.put("a", LsSimulation::handleOptionAll);
        OPTION_ACTIONS.put("A", LsSimulation::handleOptionAlmostAll);
        OPTION_ACTIONS.put("d", LsSimulation::handleOptionDirectory);
        OPTION_ACTIONS.put("F", LsSimulation::handleOptionClassify);
        OPTION_ACTIONS.put("l", LsSimulation::handleOptionLongFormat);
        OPTION_ACTIONS.put("r", LsSimulation::handleOptionReverse);
        OPTION_ACTIONS.put("S", LsSimulation::handleOptionSize);
        OPTION_ACTIONS.put("t", LsSimulation::handleOptionTime);
    }

    private static List<String> optionList = new ArrayList<>();
    private static List<String> directoryList = new ArrayList<>();
    private static String optionError = "";

    // properties to list file
    private static SortBy sortBy = SortBy.NONE;
    private static boolean showOnlyNameWithoutContent = false;
    private static boolean showAllIncludeParentDir = false;
    private static boolean showHiddenFiles = false;
    private static boolean showDetails = false;
    private static boolean showFileTypeIndicator = false;
    private static boolean reverseResult = false;

    public static void main(String[] args) {
        parseArgs(args);

        if (!optionError.isBlank()) {
            System.out.println(optionError);
            return;
        }

        // args parsed, options check ok, process to directory check
        handleDirectoryList();
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            Boolean isOption = arg.startsWith("-");

            // Handle directory in arg
            if (!isOption) {
                directoryList.add(arg);
                continue;
            }

            // Handle option in arg
            List<String> optionsFromArg = new ArrayList<>();
            Boolean isLongNameOption = arg.startsWith("--");

            if (isLongNameOption) {
                optionsFromArg.add(arg);
            } else {
                for (int i = 1; i < arg.length(); i++) {
                    optionsFromArg.add(arg.charAt(i) + "");
                }
            }

            for (String option : optionsFromArg) {
                if (!OPTION_ACTIONS.containsKey(option)) {
                    optionError = String.format(ERRORS.get("INVALID_OPTION"), option);
                    return;
                }

                optionList.add(option);
                OPTION_ACTIONS.get(option).run();
            }
        }

        // Default sort by name
        if (!showOnlyNameWithoutContent && sortBy == SortBy.NONE) {
            sortBy = SortBy.NAME;
        }

        // If no directory in arg, default is current directory
        if (directoryList.isEmpty()) {
            directoryList.add(".");
        }

        Collections.sort(directoryList);
    }

    private static void handleDirectoryList() {
        List<File> validDirectoryList = new ArrayList<>();
        List<File> fileList = new ArrayList<>();

        // Show directory error first
        for (String directoryPath : directoryList) {
            File directory = new File(directoryPath);

            if (!directory.exists()) {
                String errorMessage = String.format(ERRORS.get("NO_DIRECTORY"), directoryPath);
                System.out.println(errorMessage);
            } else {
                if (directory.isDirectory()) {
                    validDirectoryList.add(directory);
                } else {
                    fileList.add(directory);
                }
            }
        }

        // Case option -d
        if (showOnlyNameWithoutContent) {
            List<File> mergedList = new ArrayList<>(validDirectoryList);
            mergedList.addAll(fileList);

            doSort(mergedList);
            for (File item : mergedList) {
                showDetail(item);
            }

            return;
        }

        // Show files detail
        doSort(fileList);
        for (int i = 0; i < fileList.size(); i++) {
            showDetail(fileList.get(i));

            Boolean newLineAfterFileList = i == fileList.size() - 1;
            if (newLineAfterFileList) {
                System.out.println();
            }
        }

        // Show directories detail
        doSort(validDirectoryList);
        for (File directory : validDirectoryList) {
            Boolean logFolderName = directory.isDirectory() && directoryList.size() > 1;
            if (logFolderName) {
                System.out.println(directory + ":");
            }

            showDetail(directory);
        }
    }

    private static void showDetail(File directory) {
        File[] files = directory.listFiles();

        if (files == null) {
            files = new File[0];
        }

        Boolean handleOnlyDirectory = showOnlyNameWithoutContent || directory.isFile();
        if (handleOnlyDirectory) {
            files = new File[1];
            files[0] = directory;
        }

        List<File> fileList = new ArrayList<>(Arrays.asList(files));

        Boolean shouldShowCurrentAndParent = !showOnlyNameWithoutContent && directory.isDirectory()
                && showAllIncludeParentDir;
        if (shouldShowCurrentAndParent) {
            fileList.add(new File(directory, "."));
            fileList.add(new File(directory, ".."));
        }

        doSort(fileList);

        for (File file : fileList) {
            Boolean noNeedToHandle = !showOnlyNameWithoutContent && directory.isDirectory() && !showHiddenFiles
                    && file.isHidden();
            if (noNeedToHandle) {
                continue;
            }

            if (showDetails) {
                System.out.println(getFileDetails(file));
            } else {
                System.out.println(getFileName(file));
            }
        }

        Boolean newLineAfterDetails = !showOnlyNameWithoutContent && directory.isDirectory();
        if (newLineAfterDetails) {
            System.out.println();
        }
    }

    private static void doSort(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                switch (sortBy) {
                    case TIME:
                        return Long.compare(file2.lastModified(), (file1.lastModified()));
                    case SIZE:
                        return Long.compare(file2.length(), (file1.length()));
                    case NAME:
                        return file1.getName().compareTo(file2.getName());
                    default:
                        return 0;
                }
            }
        });

        if (reverseResult) {
            Collections.reverse(fileList);
        }
    }

    private static String getFileDetails(File file) {
        StringBuilder details = new StringBuilder();

        try {
            Path path = file.toPath();
            PosixFileAttributes attributes = Files.readAttributes(path, PosixFileAttributes.class);
            Set<PosixFilePermission> permissions = attributes.permissions();

            attributes.owner();
            attributes.group();

            StringBuilder permissionsString = new StringBuilder();
            permissionsString.append(attributes.isDirectory() ? "d" : "-");
            for (Map.Entry<PosixFilePermission, String> entry : PERMISSION_LIST) {
                permissionsString.append(getPermissionString(permissions, entry.getKey(), entry.getValue()));
            }
            details.append(permissionsString.toString()).append(" ");

            details.append(attributes.owner()).append(" ");
            details.append(attributes.group()).append(" ");
            details.append(attributes.size()).append(" ");

            String lastModified = formatDate(attributes.lastModifiedTime().toMillis(), "MMM dd HH:mm");
            details.append(lastModified).append(" ");

            details.append(getFileName(file)).append(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return details.toString();
    }

    private static String getFileName(File file) {
        String name = showOnlyNameWithoutContent ? file.toString() : file.getName();

        if (!showFileTypeIndicator) {
            return name;
        }

        String fileTypeIndicator = "";

        if (file.isDirectory()) {
            fileTypeIndicator = FILE_TYPE_INDICATOR.get("DIRECTORY");
        } else if (file.isFile() && file.canExecute()) {
            fileTypeIndicator = FILE_TYPE_INDICATOR.get("EXECUTABLE");
        } else if (Files.isSymbolicLink(file.toPath())) {
            fileTypeIndicator = FILE_TYPE_INDICATOR.get("SYMBOLIC_LINK");
        }

        return name + fileTypeIndicator;
    }

    private static String getPermissionString(Set<PosixFilePermission> permissions, PosixFilePermission permission,
            String symbol) {
        return permissions.contains(permission) ? symbol : "-";
    }

    private static String formatDate(long timestamp, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        return dateFormat.format(new Date(timestamp));
    }

    // Option handle methods section
    private static void handleOptionAll() {
        showAllIncludeParentDir = true;
        showHiddenFiles = true;
    }

    private static void handleOptionAlmostAll() {
        showHiddenFiles = true;
    }

    private static void handleOptionDirectory() {
        showOnlyNameWithoutContent = true;
    }

    private static void handleOptionClassify() {
        showFileTypeIndicator = true;
    }

    private static void handleOptionLongFormat() {
        showDetails = true;
    }

    private static void handleOptionReverse() {
        reverseResult = true;
    }

    private static void handleOptionSize() {
        sortBy = SortBy.SIZE;
    }

    private static void handleOptionTime() {
        sortBy = SortBy.TIME;
    }
    // End option handle methods section
}
