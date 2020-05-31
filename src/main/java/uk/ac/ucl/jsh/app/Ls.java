package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Ls extends AbstractApp {
    private String argument = "";

    public void run() throws RuntimeException {
        Path path = IntelligentPath.getPath(argument, jshCore.getCurrentDirectory());

        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Only directory path can be used as argument");
        }

        try {
            Files.list(path).forEach(this::writeFile);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }


        exit();
    }

    private void writeFile(Path fpath) {
        String fileName = fpath.getFileName().toString();

        if (!fileName.startsWith(".")) {
            writeOutputStreamLn(fileName);
        }
    }


    public void setArgs(String[] args) throws RuntimeException {
        if(args.length > 1) {
            throw new RuntimeException("Arguments do not match with the program");
        }

        if(args.length == 0) {
            argument = "";
        } else {
            argument = args[0];
        }
    }
}
