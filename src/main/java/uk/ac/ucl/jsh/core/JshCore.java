package uk.ac.ucl.jsh.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JshCore implements Core {

    private Path currentDirectory, homeDirectory;
    private String lineSeparator;
    private OutputStream outputStream;
    private InputStream inputStream;

    public JshCore() {
        currentDirectory = Paths.get(System.getProperty("user.dir"));
        homeDirectory = Paths.get(System.getProperty("user.home"));
        lineSeparator = System.getProperty("line.separator");

        outputStream = System.out;
        inputStream = System.in;
    }


    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public String getLineSeparator() { return lineSeparator; }

    public void setOutputStream(OutputStream outputStream) { this.outputStream = outputStream; }

    public void setCurrentDirectory(Path path) {
        currentDirectory = path;
    }

}
