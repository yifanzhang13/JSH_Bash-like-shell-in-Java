package uk.ac.ucl.jsh.utility;

import java.nio.file.Path;
import java.nio.file.Paths;

public class IntelligentPath {
    private static Path homeDirectory = Paths.get(System.getProperty("user.home"));
    private static String fileSeparator = System.getProperty("file.separator");

    public static Path getPath(String str, Path currentDirectory) throws RuntimeException {
        switch (str) {
            case "~":
                return homeDirectory;
            case ".":
                return currentDirectory;
            case "..":
                return currentDirectory.getParent();
        }

        return getPathOnUnix(str, currentDirectory);
    }

    private static Path getPathOnUnix(String str, Path currentDirectory) {
        if (str.startsWith("/")) {
            return Paths.get(str);
        } else if (str.startsWith("./")) {
            return Paths.get(currentDirectory.toAbsolutePath().toString() + fileSeparator + str.substring(2));
        } else if (str.startsWith("../")) {
            return Paths.get(currentDirectory.getParent().toAbsolutePath().toString() + fileSeparator + str.substring(3));
        } else {
            return Paths.get(currentDirectory.toAbsolutePath().toString() + fileSeparator + str);
        }
    }
}
