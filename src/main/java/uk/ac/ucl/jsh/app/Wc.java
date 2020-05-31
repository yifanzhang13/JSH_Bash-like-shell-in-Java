package uk.ac.ucl.jsh.app;

import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Wc extends AbstractApp{
    private List<String> files;

    private boolean countChar = false;
    private boolean countWord = false;
    private boolean countLine = false;

    private boolean fromStdIn = false;

    @Override
    public void run() throws RuntimeException {
        try {
            if (fromStdIn) {
                processStdIn();
            } else {
                processFiles();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            exit();
        }
    }

    private void processStdIn() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));

        int[] result = process(reader);
        int charCount = result[0];
        int wordCount = result[1];
        int lineCount = result[2];
        reader.close();

        printOut(charCount, wordCount, lineCount);
    }

    private void processFiles() throws IOException {
        List<Path> fileList = files.stream()
                .filter((f) -> {
                    File file = IntelligentPath.getPath(f, jshCore.getCurrentDirectory()).toFile();
                    return file.exists() && file.isFile();
                }).map((f) -> IntelligentPath.getPath(f, jshCore.getCurrentDirectory()))
                .collect(Collectors.toList());

        int charCount = 0;
        int lineCount = 0;
        int wordCount = 0;

        for (Path filePath : fileList) {
            BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
            int[] result = process(reader);
            charCount += result[0];
            wordCount += result[1];
            lineCount += result[2];
            reader.close();
        }

        printOut(charCount, wordCount, lineCount);
    }

    private int[] process(BufferedReader reader) throws IOException {
        int charCount = 0;
        int lineCount = 0;
        int wordCount = 0;

        int c;

        StringBuilder stringBuilder = new StringBuilder();
        while ((c = reader.read()) != -1) {
            charCount += 1;

            char ch = (char) c;

            if (ch == '\n') {
                lineCount += 1;
                wordCount += stringBuilder.toString().trim().split("[\\s\\xA0]+").length;
                stringBuilder = new StringBuilder();
                continue;
            }

            stringBuilder.append(ch);
        }

        return new int[] {charCount, wordCount, lineCount};
    }

    private void printOut(int charCount, int wordCount, int lineCount) {
        if (countChar) {
            writeOutputStreamLn(String.valueOf(charCount));
            return;
        }

        if (countWord) {
            writeOutputStreamLn(String.valueOf(wordCount));

            return;
        }

        if (countLine) {
            writeOutputStreamLn(String.valueOf(lineCount));
            return;
        }

        writeOutputStreamLn(String.format("%d %d %d", lineCount, wordCount, charCount));
    }

    @Override
    public void setArgs(String[] args) throws RuntimeException {
        files = new ArrayList<>();

        if (args.length == 0) {
            fromStdIn = true;
            return;
        }

        switch (args[0]) {
            case "-w":
                countWord = true;
                break;
            case "-l":
                countLine = true;
                break;
            case "-m":
                countChar = true;
                break;
            default:
                files.addAll(Arrays.asList(args[0].trim().split("[\\s\\xA0]+")));
        }

        for (int i = 1; i < args.length; i ++) {
            files.addAll(Arrays.asList(args[i].trim().split("[\\s\\xA0]+")));
        }

        if (files.size() == 0) {
            fromStdIn = true;
        }
    }
}
