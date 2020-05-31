package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.nio.file.Files;


public class Sed extends AbstractApp{
    private String regexPattern;
    private String replacement;
    private String filePath;
    private boolean isGlobal = false;

    @Override
    public void run() throws RuntimeException {
        try {
            if (filePath == null || filePath.isEmpty() || filePath.isBlank()) {
                processStdIn();
            } else {
                processFile();
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            exit();
        }
    }

    private void processFile() throws IOException {
        BufferedReader bufferedReader = Files.newBufferedReader(IntelligentPath.getPath(filePath, jshCore.getCurrentDirectory()));
        process(bufferedReader);
    }

    private void process(BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (isGlobal) {
                writeOutputStreamLn(line.replaceAll(regexPattern, replacement));
            } else {
                writeOutputStreamLn(line.replaceFirst(regexPattern, replacement));
            }
        }
    }

    private void processStdIn() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream()));
        process(bufferedReader);
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        if (args.length < 1 || args.length > 2) {
            throw new RuntimeException("sed: bad arguments");
        }

        if (args[0].length() < 2) {
            throw new RuntimeException("sed: bad replacement");
        }

        char separator = args[0].charAt(1);
        String[] replacementSplit = args[0].split(String.format("\\%c", separator));

        if (replacementSplit.length < 3 || replacementSplit.length > 4) {
            throw new RuntimeException("sed: bad replacement");
        }

        regexPattern = replacementSplit[1];
        replacement = replacementSplit[2];

        if (replacementSplit.length == 4) {
            isGlobal = true;
        }

        if (args.length == 2) {
            filePath = args[1];
        }
    }
}
